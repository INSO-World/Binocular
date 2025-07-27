package com.inso_world.binocular.cli.service

import com.inso_world.binocular.cli.index.vcs.VcsCommit
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.ffi.pojos.BinocularRepositoryPojo
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RepositoryService {
    private val logger: Logger = LoggerFactory.getLogger(RepositoryService::class.java)

    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePort

    @Autowired
    private lateinit var userPort: UserInfrastructurePort

    @Autowired
    private lateinit var commitService: CommitService

    @Autowired
    lateinit var commitPort: CommitInfrastructurePort

    @Autowired
    private lateinit var branchPort: BranchInfrastructurePort

    internal fun transformCommits(
        repo: Repository,
        commits: Iterable<VcsCommit>,
    ): Collection<Commit> {
        logger.trace(">>> transformCommits({})", repo)

        repo.user = userPort.findAll(repo).toMutableSet()
        repo.branches = branchPort.findAll(repo).toMutableSet()
        repo.commits = commitPort.findAll(repo).toMutableSet()

        val userCache =
//            userPort.findAll(repo).associateBy { it.email }.toMutableMap()
            repo.user.associateBy { it.email }.toMutableMap()
        val branchCache =
//            branchPort.findAll(repo).associateBy { it.name }.toMutableMap()
            repo.branches.associateBy { it.name }.toMutableMap()
        val commitCache: Map<String, Commit> =
//            commitPort.findAll(repo).associateBy { it.sha }
            repo.commits.associateBy { it.sha }

        // Create a map of SHA to Commit entities for quick lookups
        val commitMap =
            commits.associate {
                it.sha to
                    {
                        val e = it.toDomain()
                        val branchEntity =
                            it.branch.let { branchName ->
                                branchCache.getOrPut(branchName) {
                                    val b = Branch(name = branchName)
                                    repo.addBranch(b)
                                    b
                                }
                            }
                        branchEntity.addCommit(e)

                        repo.addCommit(e)
                        e
                    }()
            }

        // Now establish parent relationships using the map
        commitMap.forEach { (sha, commit) ->
            // Find the VcsCommit that corresponds to this entity
            val vcsCommit = commits.find { it.sha == sha }

            vcsCommit?.committer?.let {
                val email = it.email
                val user =
                    userCache.getOrPut(email) {
                        val e = it.toEntity()
                        repo.addUser(e)
                        e
                    }
                user.addCommittedCommit(commit)
            }

            vcsCommit?.author?.let {
                val email = it.email
                val user =
                    userCache.getOrPut(email) {
                        val e = it.toEntity()
                        repo.addUser(e)
                        e
                    }
                user.addAuthoredCommit(commit)
            }

            // Get the parent commits from the map and set them
            val parentCommits =
                vcsCommit?.parents?.mapNotNull { parent ->
                    commitCache[parent.sha] ?: commitMap[parent.sha]
                } ?: emptyList()

            // Set the parents on the entity
            commit.parents = parentCommits.toMutableSet()
        }

        logger.trace("<<< transformCommits({})", repo)
        return commitMap.values.toList()
    }

    private fun normalizePath(path: String): String = if (path.endsWith(".git")) path else "$path/.git"

    fun findRepo(gitDir: String): Repository? = this.repositoryPort.findByName(normalizePath(gitDir))

    private fun findRepo(vcsRepo: BinocularRepositoryPojo): Repository? = this.findRepo(vcsRepo.gitDir)

    fun getOrCreate(
        gitDir: String,
        p: Project,
    ): Repository {
        val find = this.findRepo(gitDir)
        if (find == null) {
            logger.info("Repository does not exists, creating new repository")
            return this.repositoryPort.create(
                Repository(
                    id = null,
                    name = normalizePath(gitDir),
                    project = p,
                ),
            )
        } else {
            logger.debug("Repository already exists, returning existing repository")
            return find
        }
    }

    fun getHeadCommits(
        repo: Repository,
        branch: String,
    ): Commit? = this.commitService.findHeadForBranch(repo, branch)

    fun findBranch(
        repository: Repository,
        branchName: String,
    ): Branch? {
        // Delegate the logic to the new BranchService
//        return this.branchPort.fin(repository, branchName)
        TODO()
    }

    fun update(repo: Repository): Repository = this.repositoryPort.update(repo)

    //    @Transactional
    fun addCommits(
        vcsRepo: BinocularRepositoryPojo,
        commitDtos: Collection<VcsCommit>,
        project: Project,
    ) {
        val repo = this.getOrCreate(vcsRepo.gitDir, project)
        project.repo = repo
        val existingCommitEntities = this.commitService.checkExisting(repo, commitDtos)

        logger.debug("Existing commits: ${existingCommitEntities.first.count()}")
        logger.trace("New commits to add: ${existingCommitEntities.second.count()}")

        if (existingCommitEntities.second.count() != 0) {
            // these commits are new so always added, also to an existing branch
            this.transformCommits(repo, existingCommitEntities.second)

            logger.debug("Commit transformation finished")
            logger.debug("${repo.commits.count { it.message?.isEmpty() == true }} Commits have empty messages")
            logger.trace("Empty message commits: {}", repo.commits.filter { it.message?.isEmpty() == true }.map { it.sha })

            project.repo = this.repositoryPort.update(repo)

            logger.debug("Commits successfully added. New Commit count is ${repo.commits.count()} for project ${project.name}")
        } else {
            logger.info("No new commits were found, skipping update")
        }
    }
}

fun Repository.addCommit(commit: Commit) {
    this.commits.add(commit)
    commit.repositoryId = this.id
}

private fun Repository.addUser(e: User) {
    this.user.add(e)
    e.repository = this
}

private fun Repository.addBranch(b: Branch) {
    this.branches.add(b)
    b.repositoryId = this.id
}

private fun Branch.addCommit(e: Commit) {
    this.commitShas.add(e.sha)
    e.branches.add(this)
}

// private fun User.addProjectMembership(member: ProjectMember) {
//    memberAliases.add(member)
//    member.user = this
// }
