package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.ICommitBuildConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.ICommitCommitConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.ICommitFileConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.ICommitModuleConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.ICommitUserConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IIssueCommitConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.ICommitDao
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Build
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Module
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import jakarta.annotation.PostConstruct
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.ZoneOffset

@Service
internal class CommitInfrastructurePortImpl : CommitInfrastructurePort ,
    AbstractInfrastructurePort<Commit, String>() {

    @PostConstruct
    fun init() {
        super.dao = commitDao
    }
    @Autowired private lateinit var commitDao: ICommitDao

    @Autowired private lateinit var commitBuildConnectionRepository: ICommitBuildConnectionDao

    @Autowired private lateinit var commitCommitConnectionRepository: ICommitCommitConnectionDao

    @Autowired private lateinit var commitFileConnectionRepository: ICommitFileConnectionDao

    @Autowired private lateinit var commitModuleConnectionRepository: ICommitModuleConnectionDao

    @Autowired private lateinit var issueCommitConnectionRepository: IIssueCommitConnectionDao

    @Autowired private lateinit var commitUserConnectionRepository: ICommitUserConnectionDao

    companion object {
        private val logger by logger()
    }

    override fun findAll(pageable: Pageable): Page<Commit> {
        logger.trace("Getting all commits with pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}")
        return commitDao.findAll(pageable)
    }

    override fun findAll(
        pageable: Pageable,
        since: Long?,
        until: Long?,
    ): Page<Commit> {
        logger.trace("Getting commits with pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}, since=$since, until=$until")

        if (since == null && until == null) {
            return findAll(pageable)
        }

        val allCommits = commitDao.findAll(pageable)
        val filteredCommits =
            allCommits.content.filter { commit ->
                commit.commitDateTime?.toEpochSecond(ZoneOffset.UTC)?.let { commitTime ->
                    val afterSince = since == null || commitTime >= since
                    val beforeUntil = until == null || commitTime <= until
                    afterSince && beforeUntil
                } ?: true // Include commits with null date
            }

        return Page(filteredCommits, filteredCommits.size.toLong(), pageable)
    }

    override fun findById(id: String): Commit? {
        logger.trace("Getting commit by id: $id")
        return commitDao.findById(id)
    }

    override fun findByIid(iid: Commit.Id): @Valid Commit? {
        TODO("Not yet implemented")
    }

    override fun findBuildsByCommitId(commitId: String): List<Build> {
        logger.trace("Getting builds for commit: $commitId")
        return commitBuildConnectionRepository.findBuildsByCommit(commitId)
    }

    override fun findFilesByCommitId(commitId: String): List<File> {
        logger.trace("Getting files for commit: $commitId")
        return commitFileConnectionRepository.findFilesByCommit(commitId)
    }

    override fun findModulesByCommitId(commitId: String): List<Module> {
        logger.trace("Getting modules for commit: $commitId")
        return commitModuleConnectionRepository.findModulesByCommit(commitId)
    }

    override fun findUsersByCommitId(commitId: String): List<User> {
        logger.trace("Getting users for commit: $commitId")
        return commitUserConnectionRepository.findUsersByCommit(commitId)
    }

    override fun findIssuesByCommitId(commitId: String): List<Issue> {
        logger.trace("Getting issues for commit: $commitId")
        return issueCommitConnectionRepository.findIssuesByCommit(commitId)
    }

    override fun findParentCommitsByChildCommitId(childCommitId: String): List<Commit> {
        logger.trace("Getting parent commits for child commit: $childCommitId")
        return commitCommitConnectionRepository.findParentCommits(childCommitId)
    }

    override fun findChildCommitsByParentCommitId(parentCommitId: String): List<Commit> {
        logger.trace("Getting child commits for parent commit: $parentCommitId")
        return commitCommitConnectionRepository.findChildCommits(parentCommitId)
    }

    override fun findAll(): Iterable<Commit> = this.commitDao.findAll()

    override fun create(entity: Commit): Commit = this.commitDao.save(entity)

    override fun saveAll(entities: Collection<Commit>): Iterable<Commit> = this.commitDao.saveAll(entities)

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
}
