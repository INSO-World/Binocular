package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.exception.NotFoundException
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.assembler.ProjectAssembler
import com.inso_world.binocular.infrastructure.sql.assembler.RepositoryAssembler
import com.inso_world.binocular.infrastructure.sql.mapper.AccountMapper
import com.inso_world.binocular.infrastructure.sql.mapper.IssueMapper
import com.inso_world.binocular.infrastructure.sql.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IProjectDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.AccountEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.toEntity
import com.inso_world.binocular.model.Project
import jakarta.annotation.PostConstruct
import com.inso_world.binocular.infrastructure.sql.service.AggregateFetchSupport.loadProjectEntities
import com.inso_world.binocular.model.Account
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import kotlin.getValue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Service
@Validated
internal class ProjectInfrastructurePortImpl(
    @Autowired private val projectMapper: ProjectMapper,
    @Autowired private val projectDao: IProjectDao,
) : AbstractInfrastructurePort<Project, ProjectEntity, Long>(Long::class),
    ProjectInfrastructurePort {

    companion object {
        private val logger by logger()
    }

    @Lazy
    @Autowired
    private lateinit var projectAssembler: ProjectAssembler

    @Autowired
    private lateinit var ctx: MappingContext

    @Lazy
    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePortImpl

    @Lazy
    @Autowired
    private lateinit var repositoryAssembler: RepositoryAssembler

    @Lazy
    @Autowired lateinit var accountMapper: AccountMapper

    @Lazy
    @Autowired lateinit var issueMapper: IssueMapper

    @PostConstruct
    fun init() {
        super.dao = projectDao
//        super.mapper = projectMapper
    }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findByName(name: String): Project? =
        this.projectDao.findByName(name)?.let {
            this.projectAssembler.toDomain(it)
        }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findByIid(iid: Project.Id): Project? {
        return this.projectDao.findByIid(iid)?.let {
            projectAssembler.toDomain(it)
        }
    }

    @MappingSession
    @Transactional
    override fun update(value: Project): Project {

        // Get the project entity
        val managedEntity =
            this.projectDao.findByIid(value.iid)
                ?: throw NotFoundException("Project ${value.iid} not found")

        ctx.remember(value, managedEntity)

        // update project properties (description)
        managedEntity.description = value.description

        // Manage repository:
        run {
            val domainRepo = value.repo
            val entityRepo = managedEntity.repo

            when {
                // Case 1: Both repos exist - check if they're the same and update
                domainRepo != null && entityRepo != null -> {
                    if (domainRepo.localPath != entityRepo.localPath) {
                        throw IllegalArgumentException(
                            "Cannot update project with a different repository. Project '${managedEntity.uniqueKey}' already has repository '${entityRepo.localPath}'",
                        )
                    }
                    // Don't create a new entity, just update the existing one's fields

                    repositoryPort.update(domainRepo)
                }

                // Case 2: Adding a new repo where none existed
                domainRepo != null && entityRepo == null -> {
                    managedEntity.repo = repositoryAssembler.toEntity(domainRepo)
                }

                // Case 3: Removing existing repo
                domainRepo == null && entityRepo != null -> {
                    throw UnsupportedOperationException("Deleting repository from project is not yet allowed")
                }

                // Case 4: No repo in either - nothing to do
                else -> {}
            }
        }

        // Phase 0: Map existing entities to context
        // Prevents creating duplicate entities for existing accounts/issues
        logger.trace("Mapping existing entities to context")

        // Map existing accounts
        managedEntity.accounts.forEach { accountEntity ->
            val domainAccount = value.accounts.find { it.uniqueKey == accountEntity.uniqueKey }
            if (domainAccount != null) {
                ctx.remember(domainAccount, accountEntity)
            }
        }

        // Map existing issues
        managedEntity.issues.forEach { issueEntity ->
            val domainIssue = value.issues.find { it.uniqueKey == issueEntity.uniqueKey }
            if (domainIssue != null) {
                ctx.remember(domainIssue, issueEntity)
            }
        }

        // Phase 1: Map and wire issues and accounts TODO

        // Add or update accounts
        logger.debug("Update accounts")
        value.accounts.forEach { account ->
            val accountEntity = accountMapper.toEntity(account)
            // Only add if not already present
            if (!managedEntity.accounts.contains(accountEntity)) {
                managedEntity.addAccount(accountEntity)
            }
        }
        logger.trace("Accounts updated")

        // Add or update issues
        logger.debug("Update issues")
        value.issues.forEach { issue ->
            val issueEntity = issueMapper.toEntity(issue)
            // Only add if not already present
            if (!managedEntity.issues.contains(issueEntity)) {
                managedEntity.addIssue(issueEntity)
            }

            // Add all accounts to issue
            issue.accounts.map { domainAccount ->
                val accountEntity = accountMapper.toEntity(domainAccount)
                issueEntity.addAccount(accountEntity)
            }

            // Add author connection
            issue.author?.let { domainAuthor ->
                val authorEntity = accountMapper.toEntity(domainAuthor)
                issueEntity.author = authorEntity
            }

        }
        logger.trace("Issues updated")

        val updated = super.updateEntity(managedEntity)
        logger.trace("Update executed")

        // Refresh the input domain object with persisted values and return it
        return projectMapper.refreshDomain(value, updated)
    }

    @MappingSession
    @Transactional
    override fun create(value: Project): Project {
        ensureProjectUniqueKeyAvailable(value)
        val toPersist = this.projectAssembler.toEntity(value)
        val persisted = super.create(toPersist)

        this.projectMapper.refreshDomain(value, persisted)
        return value
    }

    @MappingSession
    @Transactional
    override fun saveAll(values: Collection<Project>): Iterable<Project> {
        // Create entity-domain pairs to maintain association
        val pairs = values.map { domain -> domain to this.projectAssembler.toEntity(domain) }

        // Save all entities
        val savedEntities = this.projectDao.saveAll(pairs.map { it.second })

        // Refresh each domain object with its persisted entity and return the original collection
        pairs.zip(savedEntities).forEach { (pair, savedEntity) ->
            this.projectMapper.refreshDomain(pair.first, savedEntity)
        }

        return values
    }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findAll(): Iterable<Project> =
        loadProjectEntities(projectDao).map(projectAssembler::toDomain)

    @MappingSession
    @Transactional(readOnly = true)
    override fun findAll(pageable: Pageable): Page<Project> {
        val page = this.projectDao.findAll(pageable)
        val projects = page.content.map { this.projectAssembler.toDomain(it) }

        return Page(
            content = projects,
            totalElements = page.totalElements,
            pageable = pageable
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    @MappingSession
    @Transactional(readOnly = true)
    override fun findById(id: String): Project? =
        findByIid(Project.Id(Uuid.parse(id)))

    private fun ensureProjectUniqueKeyAvailable(project: Project) {
        val candidate = project.uniqueKey
        projectDao.findByName(candidate.name)?.let {
            throw IllegalArgumentException("Project with unique key '${candidate.name}' already exists")
        }
    }
}
