package com.inso_world.binocular.cli.service

import com.inso_world.binocular.cli.index.vcs.VcsCommit
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.ffi.pojos.BinocularRepositoryPojo
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import jakarta.transaction.Transactional
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
    private lateinit var commitService: CommitService

    @Autowired
    private lateinit var branchService: BranchService

    @Transactional
    internal fun transformCommits(
        repo: Repository,
        commits: Iterable<VcsCommit>,
    ): Collection<Commit> {
        val userCache = repo.user.associateBy { it.email }.toMutableMap()
        val branchCache = repo.branches.associateBy { it.name }.toMutableMap()
        // commitCache are the commits which exist
        val commitCache: Map<String, Commit> = repo.commits.associateBy { it.sha }

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
//                        branchEntity.commits.add(e)
//                        e.branches.add(branchEntity)
//
//                        repo.commits.add(e)
//                        e.repository = repo
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
                vcsCommit?.parents?.mapNotNull { parentSha ->
                    commitCache[parentSha] ?: commitMap[parentSha]
                } ?: emptyList()

            // Set the parents on the entity
            commit.parents = parentCommits.toMutableSet()
        }

        return commitMap.values.toList()
    }

    private fun normalizePath(path: String): String = if (path.endsWith(".git")) path else "$path/.git"

    fun findRepo(gitDir: String): Repository? = this.repositoryPort.findByName(normalizePath(gitDir))

    private fun findRepo(vcsRepo: BinocularRepositoryPojo): Repository? = this.findRepo(vcsRepo.gitDir)

//  fun findRepoWithRelations(gitDir: String): Repository? {
//    return this.repositoryDao.findByNameWithRelations(normalizePath(gitDir))
//  }

//    @Transactional
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
                    projectId = p.id,
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
        return this.branchService.findBranch(repository, branchName)
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

        // these commits are new so always added, also to an existing branch
        this.transformCommits(repo, existingCommitEntities.second)

        project.repo = this.repositoryPort.update(repo)

        logger.debug("Commits successfully added. New Commit count is ${repo.commits.count()} for project ${project.name}")
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
