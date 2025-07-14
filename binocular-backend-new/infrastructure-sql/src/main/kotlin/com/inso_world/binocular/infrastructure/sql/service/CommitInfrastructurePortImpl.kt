package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.exception.BinocularInfrastructureException
import com.inso_world.binocular.core.persistence.exception.PersistenceException
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.sql.persistence.mapper.RepositoryMapper
import com.inso_world.binocular.model.Build
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Module
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

// @Validated
@Service
internal class CommitInfrastructurePortImpl(
    @Autowired private val commitDao: ICommitDao,
    @Autowired private val commitMapper: CommitMapper,
    @Lazy private val repoMapper: RepositoryMapper,
) : AbstractInfrastructurePort<Commit, CommitEntity, Long>(Long::class),
    CommitInfrastructurePort {
    var logger: Logger = LoggerFactory.getLogger(CommitInfrastructurePort::class.java)

    @PostConstruct
    fun init() {
        super.dao = commitDao
        super.mapper = commitMapper
    }

    override fun create(domain: Commit): Commit =
        this.dao.create(commitMapper.toEntity(domain, mutableMapOf())).let {
            commitMapper.toDomain(it)
        }

    override fun findAll(
        pageable: Pageable,
        since: Long?,
        until: Long?,
    ): Page<Commit> {
        TODO("Not yet implemented")
    }

    override fun findBuildsByCommitId(commitId: String): List<Build> {
        TODO("Not yet implemented")
    }

    override fun findFilesByCommitId(commitId: String): List<File> {
        TODO("Not yet implemented")
    }

    override fun findModulesByCommitId(commitId: String): List<Module> {
        TODO("Not yet implemented")
    }

    override fun findUsersByCommitId(commitId: String): List<User> {
        TODO("Not yet implemented")
    }

    override fun findIssuesByCommitId(commitId: String): List<Issue> {
        TODO("Not yet implemented")
    }

    override fun findParentCommitsByChildCommitId(childCommitId: String): List<Commit> {
        TODO("Not yet implemented")
    }

    override fun findChildCommitsByParentCommitId(parentCommitId: String): List<Commit> {
        TODO("Not yet implemented")
    }

    override fun findExistingSha(
        repo: Repository,
        shas: List<String>,
    ): Iterable<Commit> =
        this.commitDao
            .findExistingSha(
                this.repoMapper.toEntity(repo),
                shas,
            ).map { this.commitMapper.toDomain(it) }
//            .toSet()

    override fun findAllByRepo(
        repo: Repository,
        pageable: Pageable,
    ): Iterable<Commit> {
        val repoEntity = repoMapper.toEntity(repo)
        return try {
            this.commitDao.findAllByRepo(repoEntity, pageable).map { this.commitMapper.toDomain(it) }
        } catch (e: PersistenceException) {
            throw BinocularInfrastructureException(e)
        }
    }

    override fun findHeadForBranch(
        repo: Repository,
        branch: String,
    ): Commit? =
        this.commitDao
            .findHeadForBranch(
                this.repoMapper.toEntity(repo),
                branch,
            )?.let { this.commitMapper.toDomain(it, mutableMapOf()) }

    override fun findAllLeafCommits(repo: Repository): Iterable<Commit> =
        this.commitDao
            .findAllLeafCommits(
                this.repoMapper.toEntity(repo),
            ).map { this.commitMapper.toDomain(it, mutableMapOf()) }
}
