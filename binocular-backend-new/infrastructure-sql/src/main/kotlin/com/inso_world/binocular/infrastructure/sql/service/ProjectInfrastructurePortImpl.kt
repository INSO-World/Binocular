package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.persistence.exception.NotFoundException
import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.assembler.ProjectAssembler
import com.inso_world.binocular.infrastructure.sql.assembler.RepositoryAssembler
import com.inso_world.binocular.infrastructure.sql.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IProjectDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.service.AggregateFetchSupport.loadProjectEntities
import com.inso_world.binocular.model.Project
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import kotlin.uuid.ExperimentalUuidApi

@Service
@Validated
internal class ProjectInfrastructurePortImpl(
    @Autowired private val projectMapper: ProjectMapper,
    @Autowired private val projectDao: IProjectDao,
) : AbstractInfrastructurePort<Project, ProjectEntity, Long>(Long::class),
    ProjectInfrastructurePort {
    @Lazy
    @Autowired
    private lateinit var projectAssembler: ProjectAssembler

    @Lazy
    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePortImpl

    @Lazy
    @Autowired
    private lateinit var repositoryAssembler: RepositoryAssembler

    /**
     * Self-reference to this bean's proxy instance.
     *
     * **Workaround for Spring AOP + Kotlin Value Class Issue**
     *
     * This self-injection is required to work around a limitation where Spring AOP's aspect pointcut
     * matching fails for methods with Kotlin value class parameters (inline classes) that require
     * name mangling via `@JvmName`.
     *
     * **Problem**: When a method like `findByIid(iid: Project.Id)` overrides an interface method and
     * uses a value class parameter, Kotlin mangles the JVM method name (e.g., `findByIid-pip`).
     * Spring AOP's `@annotation` pointcut cannot properly match `@MappingSession` on mangled methods,
     * causing the `MappingSessionAspect` to not be triggered.
     *
     * **Solution**: Internal method calls bypass Spring's proxy. By injecting `self` and calling
     * `self.findByIidInternal()`, we ensure the call goes through the Spring AOP proxy, allowing
     * the aspect to intercept and establish the required mapping session scope.
     *
     * @see findByIid
     * @see findByIidInternal
     */
    @Autowired
    @Lazy
    private lateinit var self: ProjectInfrastructurePortImpl

    @PostConstruct
    fun init() {
        super.dao = projectDao
    }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findByName(name: String): Project? =
        this.projectDao.findByName(name)?.let {
            this.projectAssembler.toDomain(it)
        }

    /**
     * Finds a project by its internal identifier (iid).
     *
     * **Implementation Note - Value Class Workaround**:
     * This method delegates to [findByIidInternal] via [self] (the proxy instance) to ensure
     * Spring AOP aspects are triggered. Direct implementation here would bypass the proxy due to
     * Kotlin's value class name mangling preventing proper aspect pointcut matching.
     *
     * @param iid The project's technical identifier
     * @return The project if found, null otherwise
     * @see self
     * @see findByIidInternal
     */
    override fun findByIid(iid: Project.Id): Project? {
        return self.findByIidInternal(iid)
    }

    /**
     * Internal implementation of project lookup by iid.
     *
     * **Why this method exists**:
     * This separate method is required because Spring AOP cannot intercept methods with
     * mangled signatures (caused by Kotlin value class parameters). By extracting
     * the logic here with a normal method name, Spring AOP can properly intercept the call when
     * invoked via [self], establishing the `@MappingSession` scope needed by [projectAssembler].
     *
     * **Visibility**: Must not be `private` to allow Spring CGLIB to create
     * a proxy subclass that can override this method for aspect interception.
     *
     * @param iid The project's technical identifier
     * @return The project if found, null otherwise
     * @see findByIid
     * @see MappingSession
     */
    @MappingSession
    @Transactional(readOnly = true)
    protected fun findByIidInternal(iid: Project.Id): Project? {
        return this.projectDao.findByIid(iid)?.let {
            projectAssembler.toDomain(it)
        }
    }

    @MappingSession
    @Transactional
    override fun update(value: Project): Project {
        val managedEntity =
            this.projectDao.findByIid(value.iid) ?: throw NotFoundException("Project ${value.iid} not found")

        // update project properties
        managedEntity.description = value.description

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

        val updated = super.updateEntity(managedEntity)

        // Refresh the input domain object with persisted values and return it
        this.projectMapper.refreshDomain(value, updated)
        return value
    }

    @MappingSession
    @Transactional
    override fun create(value: Project): Project {
        ensureProjectUniqueKeyAvailable(value)
        val toPersist = this.projectAssembler.toEntity(value)
        val persisted = super.create(toPersist)

        this.projectAssembler.refresh(value, persisted)
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
    override fun findById(id: String): Project? {
        TODO()
    }

    private fun ensureProjectUniqueKeyAvailable(project: Project) {
        val candidate = project.uniqueKey
        projectDao.findByName(candidate.name)?.let {
            throw IllegalArgumentException("Project with unique key '${candidate.name}' already exists")
        }
    }
}
