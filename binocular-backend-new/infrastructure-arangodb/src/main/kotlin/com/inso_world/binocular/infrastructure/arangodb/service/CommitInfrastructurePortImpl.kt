package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.infrastructure.arangodb.assembler.RepositoryAssembler
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.ICommitBuildConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.ICommitCommitConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.ICommitFileConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.ICommitModuleConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.ICommitUserConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IIssueCommitConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.ICommitDao
import com.inso_world.binocular.infrastructure.arangodb.service.BranchInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.arangodb.service.UserInfrastructurePortImpl
import com.inso_world.binocular.model.Build
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.FileOwnership
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Module
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Stats
import com.inso_world.binocular.model.User
import jakarta.annotation.PostConstruct
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.ZoneOffset

@Service
internal class CommitInfrastructurePortImpl :
    AbstractInfrastructurePort<Commit, String>(),
    CommitInfrastructurePort {
    @PostConstruct
    fun init() {
        super.dao = commitDao
    }

    @Autowired private lateinit var commitDao: ICommitDao

    @Autowired
    @Lazy
    private lateinit var repositoryAssembler: RepositoryAssembler

    @Autowired private lateinit var commitBuildConnectionRepository: ICommitBuildConnectionDao

    @Autowired private lateinit var commitCommitConnectionRepository: ICommitCommitConnectionDao

    @Autowired private lateinit var commitFileConnectionRepository: ICommitFileConnectionDao

    @Autowired private lateinit var commitModuleConnectionRepository: ICommitModuleConnectionDao

    @Autowired private lateinit var issueCommitConnectionRepository: IIssueCommitConnectionDao

    @Autowired private lateinit var commitUserConnectionRepository: ICommitUserConnectionDao

    @Autowired
    @Lazy
    private lateinit var branchPort: BranchInfrastructurePortImpl

    @Autowired
    @Lazy
    private lateinit var userPort: UserInfrastructurePortImpl

    companion object {
        private val logger by logger()
    }

    @MappingSession
    override fun findAll(pageable: Pageable): Page<Commit> {
        logger.trace("Getting all commits with pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}")
        val result = commitDao.findAll(pageable)
        populateDeveloperRepositoryChain(result.content)
        return result
    }

    /**
     * Populates the nested developer.repository chain for each commit's author and committer.
     *
     * ArangoDB's @Ref(lazy=true) on DeveloperEntity.repository means the repository reference
     * is not loaded when the developer is loaded via @Ref from CommitEntity. This method
     * ensures the developer's repository has its branches and legacy users populated, matching
     * the PostgreSQL adapter's cascading behavior.
     */
    private fun populateDeveloperRepositoryChain(commits: List<Commit>) {
        val allBranches = branchPort.findAll().toList()
        val allUsers = userPort.findAll().toList()

        val repoBranchesMap = allBranches.groupBy { it.repository.id }
        val repoUsersMap = allUsers.groupBy { it.repository.id }

        for (commit in commits) {
            for (sig in listOf(commit.authorSignature, commit.committerSignature)) {
                val dev = sig.developer
                val repoId = dev.repository.id
                repoBranchesMap[repoId]?.let { dev.repository.branches.addAll(it) }
                repoUsersMap[repoId]?.let { dev.repository.user.addAll(it) }
            }
        }
    }

    @MappingSession
    override fun findAll(
        pageable: Pageable,
        since: Long?,
        until: Long?,
    ): Page<Commit> {
        logger.trace(
            "Getting commits with pageable: page={}, size={}, since={}, until={}",
            pageable.pageNumber,
            pageable.pageSize,
            since,
            until,
        )

        if (since == null && until == null) {
            return findAll(pageable)
        }

        return findCommitsInternal(
            pageable = pageable,
            since = since,
            until = until,
        )
    }

    @MappingSession
    override fun findById(id: String): Commit? {
        logger.trace("Getting commit by id: $id")
        return commitDao.findById(id)
    }

    @MappingSession
    override fun findByIid(iid: Commit.Id): @Valid Commit? {
        TODO("Not yet implemented")
    }

    @MappingSession
    override fun findBuildsByCommitId(commitId: String): List<Build> {
        logger.trace("Getting builds for commit: $commitId")
        return commitBuildConnectionRepository.findBuildsByCommit(commitId)
    }

    @MappingSession
    override fun findFilesByCommitId(commitId: String): List<File> {
        logger.trace("Getting files for commit: $commitId")
        return commitFileConnectionRepository.findFilesByCommit(commitId)
    }

    @MappingSession
    override fun findFilesByCommitId(
        commitId: String,
        pageable: Pageable,
    ): Page<File> {
        logger.trace("Getting files for commit: $commitId with pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}")
        return commitFileConnectionRepository.findFilesByCommitPaged(commitId, pageable)
    }

    @MappingSession
    override fun findModulesByCommitId(commitId: String): List<Module> {
        logger.trace("Getting modules for commit: $commitId")
        return commitModuleConnectionRepository.findModulesByCommit(commitId)
    }

    @MappingSession
    override fun findCommitStatsByCommitId(commitId: String): Stats {
        logger.trace("Getting stats for commit: $commitId")
        return commitFileConnectionRepository.findCommitStatsByCommit(commitId)
    }

    @MappingSession
    override fun findFileStatsByCommitId(commitId: String): Map<String, Stats> {
        logger.trace("Getting per-file stats for commit: $commitId")
        return commitFileConnectionRepository.findFileStatsByCommit(commitId)
    }

    @MappingSession
    override fun findFileActionsByCommitId(commitId: String): Map<String, String?> {
        logger.trace("Getting per-file actions for commit: $commitId")
        return commitFileConnectionRepository.findFileActionsByCommit(commitId)
    }

    @MappingSession
    override fun findUsersByCommitId(commitId: String): List<User> {
        logger.trace("Getting users for commit: $commitId")
        return commitUserConnectionRepository.findUsersByCommit(commitId)
    }

    @MappingSession
    override fun findFileOwnershipByCommitAndFile(
        commitId: String,
        fileId: String,
    ): List<FileOwnership> {
        logger.trace("Getting ownership for commit: $commitId and file: $fileId")
        return commitFileConnectionRepository.findFileOwnershipByCommitAndFile(commitId, fileId)
    }

    @MappingSession
    override fun findIssuesByCommitId(commitId: String): List<Issue> {
        logger.trace("Getting issues for commit: $commitId")
        return issueCommitConnectionRepository.findIssuesByCommit(commitId)
    }

    @MappingSession
    override fun findParentCommitsByChildCommitId(childCommitId: String): List<Commit> {
        logger.trace("Getting parent commits for child commit: $childCommitId")
        return commitCommitConnectionRepository.findParentCommits(childCommitId)
    }

    @MappingSession
    override fun findChildCommitsByParentCommitId(parentCommitId: String): List<Commit> {
        logger.trace("Getting child commits for parent commit: $parentCommitId")
        return commitCommitConnectionRepository.findChildCommits(parentCommitId)
    }

    @MappingSession
    override fun findAll(): Iterable<Commit> {
        val result = this.commitDao.findAll()
        populateDeveloperRepositoryChain(result.toList())
        return result
    }

    @MappingSession
    override fun create(entity: Commit): Commit {
        repositoryAssembler.toEntity(entity.repository)
        return commitDao.save(entity)
    }

    @MappingSession
    override fun saveAll(entities: Collection<Commit>): Iterable<Commit> {
        entities.forEach { create(it) }
        return entities
    }

    override fun update(entity: Commit): Commit {
        TODO("Not yet implemented")
    }

    override fun findExistingSha(
        repo: Repository,
        shas: List<String>,
    ): Set<Commit> {
        TODO("Not yet implemented")
    }

    override fun findAll(
        repo: Repository,
        pageable: Pageable,
    ): Iterable<Commit> {
        TODO("Not yet implemented")
    }

    override fun findHeadForBranch(
        repo: Repository,
        branch: String,
    ): Commit? {
        TODO("Not yet implemented")
    }

    override fun findAllLeafCommits(repo: Repository): Iterable<Commit> {
        TODO("Not yet implemented")
    }

    override fun findAll(repo: Repository): Iterable<Commit> {
        TODO("Not yet implemented")
    }

    // TODO: do in db, same as for commit controller
    private fun findCommitsInternal(
        pageable: Pageable,
        since: Long?,
        until: Long?,
    ): Page<Commit> {
        fun Commit.commitMillis() = commitDateTime?.toInstant(ZoneOffset.UTC)?.toEpochMilli()

        val comparatorAsc: Comparator<Commit> =
            compareBy(
                { it.commitMillis() },
                { it.sha }
            )

        val filteredAndSorted =
            commitDao
                .findAll()
                .asSequence()
                .filter { commit ->
                    val ts = commit.commitMillis() ?: return@filter true
                    (since == null || ts >= since) &&
                        (until == null || ts <= until)
                }.sortedWith(comparatorAsc)
                .toList()

        val from =
            (pageable.pageNumber * pageable.pageSize)
                .coerceAtMost(filteredAndSorted.size)
        val to =
            (from + pageable.pageSize)
                .coerceAtMost(filteredAndSorted.size)

        return Page(
            content = filteredAndSorted.subList(from, to),
            totalElements = filteredAndSorted.size.toLong(),
            pageable = pageable
        )
    }
}
