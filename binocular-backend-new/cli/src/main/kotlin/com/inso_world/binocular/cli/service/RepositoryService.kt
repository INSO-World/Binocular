package com.inso_world.binocular.cli.service

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Repository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Paths

/**
 * Service for managing repository operations including commit canonicalization and persistence.
 *
 * This service handles the coordination between the Git indexer output (commits with their
 * signatures and developers already set) and the persistence layer. With the new domain model
 * using [Developer] and [com.inso_world.binocular.model.Signature], commits come pre-built
 * with immutable author/committer information.
 *
 * ## Key Responsibilities
 * - Canonicalize commits against existing repository state
 * - Wire parent-child relationships
 * - Deduplicate developers by git signature
 * - Persist repository changes
 *
 * ## Domain Model Integration
 * The new domain model uses ID-based references:
 * - [Commit] has [Commit.parentIds] and [Commit.childIds] (ID sets)
 * - [Repository] has [Repository.commitIds], [Repository.branchIds], [Repository.remoteIds]
 * - [Developer] is global (no repository scope)
 *
 * This means transformCommits focuses on deduplication and parent wiring rather than
 * establishing back-links.
 */
@Service
class RepositoryService {
    companion object {
        private val logger by logger()
    }

    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePort

    @Autowired
    private lateinit var commitService: CommitService

    fun findBranch(repo: Repository, name: String): Branch? {
        return this.repositoryPort.findBranch(repo, name)
    }

    @Transactional
    fun save(repository: Repository): Repository {
        return this.repositoryPort.create(repository)
    }

    /**
     * Canonicalizes incoming commits against the repository's existing state.
     *
     * With the new domain model where [Commit]s use immutable [com.inso_world.binocular.model.Signature]s,
     * this method focuses on:
     * 1. Deduplicating commits by SHA (uniqueKey)
     * 2. Wiring parent-child relationships between commits
     *
     * @param repo The repository to canonicalize commits into
     * @param commits The incoming commits (already with signatures set)
     * @return The canonicalized commits corresponding to input order
     */
    internal fun transformCommits(
        repo: Repository,
        commits: Iterable<Commit>,
    ): Collection<Commit> {
        // Build index of canonical commits from repository by SHA
        val commitsBySha = mutableMapOf<String, Commit>()

        // --- Pass 1: Ensure every incoming commit has a canonical instance ---
        val canonicalInOrder = commits.map { incoming ->
            val existing = commitsBySha[incoming.sha]
            if (existing != null) {
                existing
            } else {
                commitsBySha[incoming.sha] = incoming
                repo.commitIds.add(incoming.iid)
                incoming
            }
        }

        // --- Pass 2: Wire parent-child relationships ---
        canonicalInOrder.forEach { canonicalCommit ->
            // Wire parents from incoming commit's parentIds list
            canonicalCommit.parentIds.forEach { parentId ->
                val canonicalParent = commitsBySha.values.find { it.iid == parentId }
                if (canonicalParent != null && !canonicalCommit.parentIds.contains(parentId)) {
                    canonicalCommit.parentIds.add(parentId)
                    canonicalParent.childIds.add(canonicalCommit.iid)
                }
            }

            // Wire children from incoming commit's childIds list (if any)
            canonicalCommit.childIds.forEach { childId ->
                val canonicalChild = commitsBySha.values.find { it.iid == childId }
                if (canonicalChild != null && !canonicalCommit.childIds.contains(childId)) {
                    canonicalCommit.childIds.add(childId)
                    canonicalChild.parentIds.add(canonicalCommit.iid)
                }
            }
        }

        return canonicalInOrder
    }

    // ---------- Utilities ----------

    private fun normalizePath(path: String): String =
        (if (path.endsWith(".git")) path else "$path/.git").let {
            Paths.get(it).toRealPath().toString()
        }

    /**
     * Find a repository by its normalized git directory path.
     */
    fun findRepo(gitDir: String): Repository? = this.repositoryPort.findByName(normalizePath(gitDir))

    /**
     * Create a new repository in the persistence layer.
     *
     * @throws IllegalArgumentException if repository.id is not null (already persisted)
     */
    fun create(repository: Repository): Repository {
        require(repository.id == null) { "Repository.id must be null to create repository" }

        return this.repositoryPort.create(repository)
    }

    /**
     * Get the HEAD commit for a specific branch.
     */
    fun getHeadCommits(
        repo: Repository,
        branch: String,
    ): Commit? = this.commitService.findHeadForBranch(repo, branch)

    /**
     * Add commits to a repository, checking for existing commits first.
     *
     * This method:
     * 1. Checks which commits already exist in the repository
     * 2. Canonicalizes new commits (wires relationships)
     * 3. Persists the updated repository
     *
     * @param repo The repository to add commits to
     * @param commits The commits to add
     * @return The updated repository
     */
    fun addCommits(
        repo: Repository,
        commits: Collection<Commit>,
    ): Repository {
        val existingCommitEntities = this.commitService.checkExisting(repo, commits)

        logger.debug("Existing commits: ${existingCommitEntities.first.count()}")
        logger.trace("New commits to add: ${existingCommitEntities.second.count()}")

        // Canonicalize new commits and wire relationships
        this.transformCommits(repo, existingCommitEntities.second)

        logger.debug("Commit transformation finished")
        logger.debug("New Commit count is ${repo.commitIds.count()}")

        val newRepo = this.repositoryPort.update(repo)

        return newRepo
    }

}
