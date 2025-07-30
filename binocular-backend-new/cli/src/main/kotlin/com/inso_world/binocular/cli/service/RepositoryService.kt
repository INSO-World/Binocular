package com.inso_world.binocular.cli.service

import com.inso_world.binocular.cli.index.vcs.VcsCommit
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.ffi.pojos.BinocularRepositoryPojo
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
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

    internal fun transformCommits(
        repo: Repository,
        commits: Iterable<VcsCommit>,
    ): Collection<Commit> {
        logger.trace(">>> transformCommits({})", repo)

//        repo.user = userPort.findAll(repo).toMutableSet()
//        repo.branches = branchPort.findAll(repo).toMutableSet()
//        repo.commits = commitPort.findAll(repo).toMutableSet()

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
                it.sha to it.toDomain()
            }

        // Now establish parent relationships using the map
        commitMap.forEach { (sha, commit) ->
            // Find the VcsCommit that corresponds to this entity
            val vcsCommit = commits.find { it.sha == sha }

            run {
                val branchEntity =
                    vcsCommit?.branch?.let { branchName ->
                        branchCache.getOrPut(branchName) {
                            val b = Branch(name = branchName)
                            repo.branches.add(b)
                            b
                        }
                    }
                branchEntity?.commits?.add(commit)
            }

            vcsCommit?.committer?.let {
                val email = it.email
                val user =
                    userCache.getOrPut(email) {
                        val e = it.toEntity()
                        repo.user.add(e)
                        e
                    }
                user.committedCommits.add(commit)
            }

            vcsCommit?.author?.let {
                val email = it.email
                val user =
                    userCache.getOrPut(email) {
                        val e = it.toEntity()
                        repo.user.add(e)
                        e
                    }
                user.authoredCommits.add(commit)
            }

            // Get the parent commits from the map and set them
            val parentCommits =
                vcsCommit?.parents?.mapNotNull { parent ->
                    commitCache[parent.sha] ?: commitMap[parent.sha]
                } ?: emptyList()

            // Set the parents on the entity
            commit.parents.addAll(parentCommits)
            repo.commits.add(commit)
        }

        logger.trace("<<< transformCommits({})", repo)
        return commitMap.values.toList()
    }

    private fun normalizePath(path: String): String = if (path.endsWith(".git")) path else "$path/.git"

    fun findRepo(gitDir: String): Repository? = this.repositoryPort.findByName(normalizePath(gitDir))

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

    fun update(repo: Repository): Repository = this.repositoryPort.update(repo)

    //    @Transactional
    fun addCommits(
        vcsRepo: BinocularRepositoryPojo,
        commitDtos: Collection<VcsCommit>,
        project: Project,
    ) {
        val repo =
            project.repo ?: run {
                val repo = this.getOrCreate(vcsRepo.gitDir, project)
                project.repo = repo
                repo
            }
        val existingCommitEntities = this.commitService.checkExisting(repo, commitDtos)

        logger.debug("Existing commits: ${existingCommitEntities.first.count()}")
        logger.trace("New commits to add: ${existingCommitEntities.second.count()}")

        if (existingCommitEntities.second.count() != 0) {
            // these commits are new so always added, also to an existing branch
            this.transformCommits(repo, existingCommitEntities.second)

            logger.debug("Commit transformation finished")
            logger.debug("${repo.commits.count { it.message?.isEmpty() == true }} Commits have empty messages")
            logger.trace(
                "Empty message commits: {}",
                repo.commits.filter { it.message?.isEmpty() == true }.map { it.sha },
            )

            project.repo = this.repositoryPort.update(repo)

            logger.debug("Commits successfully added. New Commit count is ${repo.commits.count()} for project ${project.name}")
        } else {
            logger.info("No new commits were found, skipping update")
        }
    }
}
