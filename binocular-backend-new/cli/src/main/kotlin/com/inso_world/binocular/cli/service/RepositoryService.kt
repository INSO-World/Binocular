package com.inso_world.binocular.cli.service

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Paths

@Service
class RepositoryService {
    companion object {
        private val logger by logger()
    }

    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePort

    @Autowired
    private lateinit var commitService: CommitService

    /**
     * Deduplicate incoming commits against the repository by uniqueKey() and
     * wire up relationships (author/committer, branches, parents/children)
     * so that all references point to the canonical objects in `repo`.
     *
     * Returns the canonicalized commits corresponding to the input order.
     */
    internal fun transformCommits(
        repo: Repository,
        commits: Iterable<Commit>,
    ): Collection<Commit> {
        // --- canonical indexes from repository state
        val commitsByKey = repo.commits.associateByTo(mutableMapOf<Commit.Key, Commit>()) { it.uniqueKey }
        val usersByKey = repo.user.associateByTo(mutableMapOf<User.Key, User>()) { it.uniqueKey }
        val usersByEmail =
            repo.user
                .mapNotNull { u -> normalizeEmail(u.email)?.let { it to u } }
                .toMap(mutableMapOf())

        val branchesByKey = repo.branches.associateByTo(mutableMapOf<Branch.Key, Branch>()) { it.uniqueKey }

        // --- seed indexes with any pre-attached users on incoming commits
        commits.forEach { c ->
            listOf(c.author, c.committer).forEach { u ->
                if (u != null) {
                    usersByKey.putIfAbsent(u.uniqueKey, u)
                    normalizeEmail(u.email)?.let { usersByEmail.putIfAbsent(it, u) }
                }
            }
        }

        fun canonicalizeCommit(incoming: Commit): Commit {
            val key = incoming.uniqueKey
            val existing = commitsByKey[key]
            if (existing != null) return existing

            // Add to repo first to establish repository back-link
            repo.commits.add(incoming)
            commitsByKey[key] = incoming
            return incoming
        }

        fun canonicalizeUser(u: User?): User? {
            if (u == null) return null

            // 1) prefer uniqueKey match
            usersByKey[u.uniqueKey]?.let { return it }

            // 2) coalesce by email (case-insensitive)
            normalizeEmail(u.email)?.let { em ->
                usersByEmail[em]?.let { existing -> return existing }
            }

            // 3) no match — register new canonical instance
            repo.user.add(u)
            usersByKey[u.uniqueKey] = u
            normalizeEmail(u.email)?.let { usersByEmail[it] = u }
            return u
        }

        fun canonicalizeBranch(b: Branch?): Branch? {
            if (b == null) return null
            return branchesByKey[b.uniqueKey] ?: run {
                repo.branches.add(b)
                branchesByKey[b.uniqueKey] = b
                b
            }
        }

        // Helpers to *safely* rebind author/committer to canonical instances
        fun forceSetAuthor(
            c: Commit,
            target: User?,
        ) {
            if (target == null) return
            val current = c.author
            if (current === target) return
//            if (current != null && sameEmail(current, target)) {
//                // migrate to canonical: remove old back-link, then use setter
//                current.authoredCommits.remove(c)
//                // clear via reflection to allow setter without violating guard
//                setField(c, "author", null)
//            }
            if (c.author == null) c.author = target
        }

        fun forceSetCommitter(
            c: Commit,
            target: User?,
        ) {
            if (target == null) return
            val current = c.committer
            if (current === target) return
//            if (current != null && sameEmail(current, target)) {
//                current.committedCommits.remove(c)
//                setField(c, "committer", null)
//            }
//            if (c.committer == null) c.committer = target
        }

        // --- pass 1: ensure every commit has a canonical instance
        val canonicalInOrder = commits.map { canonicalizeCommit(it) }

        // --- pass 2: users + branches
        commits.forEach { raw ->
            val c = canonicalizeCommit(raw)

            val authorCanon = canonicalizeUser(raw.author)
            val committerCanon = canonicalizeUser(raw.committer)

            // If both emails are equal, unify to the same instance (prefer author’s instance)
            val unifiedByEmail =
                if (authorCanon != null && committerCanon != null &&
                    sameEmail(authorCanon, committerCanon)
                ) {
                    authorCanon
                } else {
                    null
                }

            forceSetAuthor(c, unifiedByEmail ?: authorCanon)
            forceSetCommitter(c, unifiedByEmail ?: committerCanon)

//            raw.branches.forEach { bRaw ->
//                canonicalizeBranch(bRaw)?.commits?.add(c)
//            }
        }

        // --- pass 3: parents / children
        commits.forEach { raw ->
            val c = canonicalizeCommit(raw)

            raw.parents.forEach { pRaw ->
                val p = canonicalizeCommit(pRaw)
                c.parents.add(p) // back-links to children are handled by domain model
            }
            raw.children.forEach { chRaw ->
                val ch = canonicalizeCommit(chRaw)
                c.children.add(ch)
            }
        }

        return canonicalInOrder
    }

    // ---------- small utilities ----------

    private fun normalizeEmail(email: String?): String? = email?.trim()?.lowercase()

    private fun sameEmail(
        a: User,
        b: User,
    ): Boolean =
        normalizeEmail(a.email) != null &&
            normalizeEmail(a.email) == normalizeEmail(b.email)

    private fun normalizePath(path: String): String =
        (if (path.endsWith(".git")) path else "$path/.git").let {
            Paths.get(it).toRealPath().toString()
        }

    fun findRepo(gitDir: String): Repository? = this.repositoryPort.findByName(normalizePath(gitDir))

    fun create(repository: Repository): Repository {
        require(repository.id == null) { "Repository.id must be null to create repository" }
        require(repository.project != null) { "Repository.project must not be null to create repository" }
        require(repository.project?.repo == repository) { "Mismatch in Repository and Project configuration" }

//        val find = this.findRepo(name)
//        if (find == null) {
//            logger.info("Repository does not exists, creating new repository")
        return this.repositoryPort.create(repository)
//        } else {
//            logger.debug("Repository already exists, returning existing repository")
//            return find
//        }
    }

    fun getHeadCommits(
        repo: Repository,
        branch: String,
    ): Commit? = this.commitService.findHeadForBranch(repo, branch)

    fun update(repo: Repository): Repository = this.repositoryPort.update(repo)

    //    @Transactional
    fun addCommits(
        repo: Repository,
        commits: Collection<Commit>,
    ): Repository {
        val existingCommitEntities = this.commitService.checkExisting(repo, commits)

        logger.debug("Existing commits: ${existingCommitEntities.first.count()}")
        logger.trace("New commits to add: ${existingCommitEntities.second.count()}")

        if (existingCommitEntities.second.isNotEmpty()) {
            // these commits are new so always added, also to an existing branch
            this.transformCommits(repo, existingCommitEntities.second)

            logger.debug("Commit transformation finished")
            logger.debug("${repo.commits.count { it.message?.isEmpty() == true }} Commits have empty messages")
            logger.trace(
                "Empty message commits: {}",
                repo.commits.filter { it.message?.isEmpty() == true }.map { it.sha },
            )

            val newRepo = update(repo)

            logger.debug("Commits successfully added. New Commit count is ${repo.commits.count()} for project ${repo.project.name}")
            return newRepo
        } else {
            logger.info("No new commits were found, skipping update")
            return repo
        }
    }

}
