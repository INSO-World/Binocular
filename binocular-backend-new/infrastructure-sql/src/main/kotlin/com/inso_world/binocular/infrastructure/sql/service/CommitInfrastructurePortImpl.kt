package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.exception.BinocularInfrastructureException
import com.inso_world.binocular.core.persistence.exception.PersistenceException
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.exception.NotFoundException
import com.inso_world.binocular.infrastructure.sql.assembler.RepositoryAssembler
import com.inso_world.binocular.infrastructure.sql.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.sql.mapper.DeveloperMapper
import com.inso_world.binocular.infrastructure.sql.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.sql.persistence.dao.CommitDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.DeveloperDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.RepositoryDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.DeveloperEntity
import com.inso_world.binocular.model.Build
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Module
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import jakarta.annotation.PostConstruct
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import java.util.stream.Collectors
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Service
@Validated
@Deprecated("Use domain aggregate `Repository`")
internal class CommitInfrastructurePortImpl
@Autowired constructor(
    private val commitMapper: CommitMapper,
    private val commitDao: CommitDao,
    @Lazy private val developerDao: DeveloperDao,
    @Lazy private val repositoryDao: RepositoryDao,
) : AbstractInfrastructurePort<Commit, CommitEntity, Long>(Long::class), CommitInfrastructurePort {
    @Autowired
    private lateinit var repositoryAssembler: RepositoryAssembler
    var logger: Logger = LoggerFactory.getLogger(CommitInfrastructurePort::class.java)

    @Autowired
    @Lazy
    private lateinit var projectMapper: ProjectMapper

    @Autowired
    @Lazy
    private lateinit var repositoryMapper: RepositoryMapper

    @Autowired
    private lateinit var ctx: MappingContext

    @Autowired
    @Lazy
    private lateinit var developerMapper: DeveloperMapper

    @PostConstruct
    fun init() {
        super.dao = commitDao
    }

    @MappingSession
    @Transactional
    override fun create(value: Commit): Commit {
        val repositoryEntity = repositoryDao.findByIid(value.repository.iid)
            ?: throw NotFoundException("Repository ${value.repository.iid} not found")

        ctx.remember(value.repository, repositoryEntity)
        val mapped = commitMapper.toEntity(value)
        return this.commitDao.create(mapped).let { commitEntity ->
            commitMapper.refreshDomain(value, commitEntity)
        }
    }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findAll(): Iterable<Commit> {
        val commits = this.commitDao.findAll()

        // Group commits by repository to process related commits together
        return commits.groupBy { it.repository }.flatMap { (repoEntity, _) ->
            repositoryAssembler.toDomain(repoEntity).commits
        }
    }

    override fun findAll(pageable: Pageable): Page<Commit> {
        TODO("Not yet implemented")
    }

    @OptIn(ExperimentalUuidApi::class)
    @MappingSession
    @Transactional(readOnly = true)
    override fun findById(id: String): Commit? = this.commitDao.findByIid(Commit.Id(Uuid.parse(id)))?.let {
        val repository = it.repository.let { r ->
            val project = projectMapper.toDomain(
                r.project,
            )

            repositoryMapper.toDomain(r)
        }

        commitMapper.toDomain(it)
    }

    override fun findByIid(iid: Commit.Id): @Valid Commit? {
        TODO("Not yet implemented")
    }

    @MappingSession
    @Transactional
    override fun update(value: Commit): Commit {
        val repositoryEntity = repositoryDao.findByIid(value.repository.iid)
            ?: throw NotFoundException("Repository ${value.repository} not found")

        ctx.remember(value.repository, repositoryEntity)

        val entity = this.commitDao.findBySha(repositoryEntity, value.sha)
            ?: throw NotFoundException("Commit ${value.sha} not found, required for update")

//        entity.authorDateTime = value.authorDateTime
//        entity.commitDateTime = value.commitDateTime
        entity.apply {
            this.message = value.message
            this.webUrl = value.webUrl
            this.authorDateTime = value.authorSignature.timestamp
            this.commitDateTime = (value.committerSignature ?: value.authorSignature).timestamp
            this.committer = resolveDeveloperEntity(value.committer)
            this.author = resolveDeveloperEntity(value.author)
        }

        return this.commitDao.update(entity).let {
            commitMapper.refreshDomain(value, it)
        }
    }

    private fun resolveDeveloperEntity(developer: Developer): DeveloperEntity {
        ctx.findEntity<Developer.Key, Developer, DeveloperEntity>(developer)?.let { return it }

        developer.id?.trim()?.toLongOrNull()?.let { existingId ->
            developerDao.findById(existingId)?.let { found ->
                ctx.remember(developer, found)
                return found
            }
        }

        val entity = developerMapper.toEntity(developer)
        return developerDao.create(entity)
    }

    override fun saveAll(values: Collection<Commit>): Iterable<Commit> {
        TODO("Not yet implemented")
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

    override fun findUsersByCommitId(commitId: String): List<User> = emptyList()

    override fun findIssuesByCommitId(commitId: String): List<Issue> {
        TODO("Not yet implemented")
    }

    override fun findParentCommitsByChildCommitId(childCommitId: String): List<Commit> {
        TODO("Not yet implemented")
    }

    override fun findChildCommitsByParentCommitId(parentCommitId: String): List<Commit> {
        TODO("Not yet implemented")
    }

    @Transactional(readOnly = true)
    @MappingSession
    override fun findExistingSha(
        repo: Repository,
        shas: List<String>,
    ): Iterable<Commit> {
        return this.commitDao.findExistingSha(repo, shas).map {
            this.commitMapper.toDomain(it)
        }
    }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findAll(
        repo: Repository,
        pageable: Pageable,
    ): Iterable<Commit> {
        return try {
            this.repositoryDao.findByIid(repo.iid)?.let {
                ctx.remember(repo, it)
                it.commits
            }?.map { this.commitMapper.toDomain(it) } ?: emptyList()
        } catch (e: PersistenceException) {
            throw BinocularInfrastructureException(e)
        }
    }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findAll(repository: Repository): Iterable<Commit> =
        this.commitDao.findAll(repository).collect(Collectors.toSet()).map {
            return@map this.commitMapper.toDomain(it)
        }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findHeadForBranch(
        repo: Repository,
        branch: String,
    ): Commit? {
        return this.repositoryDao.findByIid(
            repo.iid
        )?.let {
            ctx.remember(repo, it)
            it
        }?.let {
            this.commitDao.findHeadForBranch(it, branch)
        }?.let { head -> return@let repo.commits.associateBy(Commit::iid).getValue(head.iid) }
    }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findAllLeafCommits(repo: Repository): Iterable<Commit> {
        return this.repositoryDao.findByIid(repo.iid)?.let {
            ctx.remember(repo, it)
            this.commitDao.findAllLeafCommits(it)
        }?.map { this.commitMapper.toDomain(it) } ?: emptyList()
    }
}
