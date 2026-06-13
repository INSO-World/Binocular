package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.exception.NotFoundException
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.assembler.RepositoryAssembler
import com.inso_world.binocular.infrastructure.sql.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.sql.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.sql.persistence.dao.BranchDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.CommitDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.ProjectDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.RepositoryDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import jakarta.annotation.PostConstruct
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

@Service
@Validated
internal class RepositoryInfrastructurePortImpl :
    AbstractInfrastructurePort<Repository, RepositoryEntity, Long>(Long::class),
    RepositoryInfrastructurePort {
    companion object {
        private val logger by logger()
    }

    /**
     * Self-reference to this bean's proxy instance.
     *
     * **Workaround for Spring AOP + Kotlin Value Class Issue**
     *
     * This self-injection is required to work around a limitation where Spring AOP's aspect pointcut
     * matching fails for methods with Kotlin value class parameters (inline classes) that require
     * name mangling via `@JvmName`.
     *
     * **Problem**: When a method like `findByIid(iid: Repository.Id)` overrides an interface method and
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
    private lateinit var self: RepositoryInfrastructurePortImpl

    @Autowired
    private lateinit var ctx: MappingContext

    @Autowired
    private lateinit var branchDao: BranchDao

    @Autowired
    @Lazy
    lateinit var commitMapper: CommitMapper

    @Autowired
    private lateinit var repositoryAssembler: RepositoryAssembler

    @Autowired
    private lateinit var repositoryMapper: RepositoryMapper

    @Autowired
    private lateinit var repositoryDao: RepositoryDao

    @Autowired
    @Lazy
    private lateinit var commitDao: CommitDao

    @Autowired
    private lateinit var projectDao: ProjectDao

    @Autowired
    private lateinit var projectMapper: ProjectMapper

    @PostConstruct
    fun init() {
        super.dao = repositoryDao
    }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findByName(name: String): Repository? =
        this.repositoryDao.findByName(name)?.let {
            this.repositoryAssembler.toDomain(it)
        }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findAll(): Iterable<Repository> = this.repositoryDao.findAll().map(repositoryAssembler::toDomain)

    @MappingSession
    @Transactional(readOnly = true)
    override fun findAll(pageable: Pageable): Page<Repository> {
        val page = this.repositoryDao.findAll(pageable)
        val repositories = page.content.map { this.repositoryAssembler.toDomain(it) }

        return Page(
            content = repositories,
            totalElements = page.totalElements,
            pageable = pageable
        )
    }

    /**
     * Finds a repository by its internal identifier (iid).
     *
     * **Implementation Note - Value Class Workaround**:
     * This method delegates to [findByIidInternal] via [self] (the proxy instance) to ensure
     * Spring AOP aspects are triggered. Direct implementation here would bypass the proxy due to
     * Kotlin's value class name mangling preventing proper aspect pointcut matching.
     *
     * @param iid The repository's technical identifier
     * @return The repository if found, null otherwise
     * @see self
     * @see findByIidInternal
     */
    override fun findByIid(iid: Repository.Id): Repository? = self.findByIidInternal(iid)

    /**
     * Internal implementation of repository lookup by iid.
     *
     * **Why this method exists**:
     * This separate method is required because Spring AOP cannot intercept methods with
     * mangled signatures (caused by Kotlin value class parameters). By extracting
     * the logic here with a normal method name, Spring AOP can properly intercept the call when
     * invoked via [self], establishing the `@MappingSession` scope needed by [repositoryAssembler].
     *
     * **Visibility**: Must not be `private` to allow Spring CGLIB to create
     * a proxy subclass that can override this method for aspect interception.
     *
     * @param iid The repository's technical identifier
     * @return The repository if found, null otherwise
     * @see findByIid
     * @see MappingSession
     */
    @MappingSession
    @Transactional(readOnly = true)
    protected fun findByIidInternal(iid: Repository.Id): Repository? =
        this.repositoryDao.findByIid(iid)?.let {
            repositoryAssembler.toDomain(it)
        }

    @MappingSession
    @Transactional
    override fun create(
        @Valid value: Repository,
    ): Repository {
        val projectEntity =
            projectDao.findByIid(value.project.iid) ?: throw NotFoundException("Project ${value.project} not found")

        if (projectEntity.repo != null) {
            throw IllegalArgumentException("Selected project $projectEntity has already a Repository set")
        }

        ctx.remember(value.project, projectEntity)

        val toPersist = this.repositoryAssembler.toEntity(value)
        val persisted = super.create(toPersist)

        // Refresh the input domain object with persisted values and return it
        this.repositoryMapper.refreshDomain(value, persisted)
        return value
    }

    /**
     * Updates an existing [Repository] aggregate, persisting all owned children (commits, branches,
     * developers, remotes) and upserting the parent [com.inso_world.binocular.model.Project] reference if it does not yet exist.
     *
     * ## Semantics
     * 1. Resolve the parent `ProjectEntity` by `value.project.iid`. If absent, persist it first
     *    (upsert) so the repository can reference a valid `fk_project_id`. Either way,
     *    `value.project.id` is then refreshed from the resolved entity (a no-op when it was
     *    already correct).
     * 2. Assemble the full `RepositoryEntity` graph via [RepositoryAssembler.toEntity].
     * 3. **Phase 1** — persist commits (parent/child links and branches temporarily detached).
     * 4. **Phase 2** — re-wire commit parent/child relationships now that commits have ids.
     * 5. **Phase 3** — re-attach branches (pointing at persisted commit heads) and flush.
     * 6. Refresh `value` from the persisted state via [RepositoryAssembler.refresh].
     *
     * ## Invariants & Requirements
     * - **New repository (`mapped.id == null`)**: must be persisted via `entityManager.persist(mapped)`,
     *   never via [RepositoryDao.update] (= `entityManager.merge`). `RepositoryEntity`'s
     *   `init { project.repo = this }` side effect sets the already-managed parent `ProjectEntity.repo`
     *   to `mapped`. Since `ProjectEntity.repo` is declared `cascade = [CascadeType.ALL]`, merging
     *   `mapped` would persist a *separate* managed copy while leaving the original (still-transient)
     *   `mapped` reachable from `project.repo` — Hibernate then cascade-persists that stale instance
     *   too, producing a second `repositories` row for the same `fk_project_id` and violating
     *   `uc_repositories_fk_project_id`. Using `persist()` makes `mapped` itself the managed instance,
     *   so `project.repo` and the persisted entity are identical and no duplicate insert occurs.
     * - **Existing repository (`mapped.id != null`)**: persisted via [RepositoryDao.update]
     *   (`entityManager.merge`), unchanged from prior behavior.
     *
     * @param value The repository aggregate to update (refreshed in place with persisted ids).
     * @return The same [value] instance, refreshed with persisted identifiers.
     */
    @MappingSession
    @Transactional
    override fun update(
        @Valid value: Repository,
    ): Repository {
        val entity =
            projectDao.findByIid(value.project.iid)
                ?: run {
                    logger.debug("Project {} not found on update — persisting it (upsert)", value.project.uniqueKey)
                    projectDao.create(projectMapper.toEntity(value.project))
                }
        logger.debug("Project Entity found")
        ctx.remember(value.project, entity)
        projectMapper.refreshDomain(value.project, entity)

        val mapped = repositoryAssembler.toEntity(value)

        // Phase 1: Save commits first (without branches and without parent/child relationships)
        // This ensures commits have database IDs before branches reference them
        val branches = mapped.branches.toMutableSet()
        mapped.branches.clear()

        // Store and clear parent/child relationships
        val commitParentMap = mapped.commits.associateWith { it.parents.toSet() }
        mapped.commits.forEach {
            it.parents.clear()
            it.children.clear()
        }

        // Persist commits.
        // New repositories (mapped.id == null) must use entityManager.persist(mapped) directly
        // rather than repositoryDao.update (= entityManager.merge): see KDoc on this method for why.
        val intermediateRepo =
            if (mapped.id == null) {
                entityManager.persist(mapped)
                mapped
            } else {
                repositoryDao.update(mapped)
            }
        entityManager.flush()
        logger.trace("Phase 1: Commits persisted")

        // Phase 2: Wire parent/child relationships (commits now have IDs)
        val commitsBySha = intermediateRepo.commits.associateBy { it.sha }
        intermediateRepo.commits.forEach { commitEntity ->
            // Find the original entity in the map to get its parents
            val originalEntity = mapped.commits.find { it.sha == commitEntity.sha }
            val originalParents = originalEntity?.let { commitParentMap[it] } ?: emptySet()

            originalParents.forEach { parentEntity ->
                val persistedParent = commitsBySha[parentEntity.sha]
                if (persistedParent != null && !commitEntity.parents.contains(persistedParent)) {
                    commitEntity.parents.add(persistedParent)
                    persistedParent.children.add(commitEntity)
                }
            }
        }
        entityManager.flush()
        logger.trace("Phase 2: Parent/child relationships wired")

        // Phase 3: Add branches (commits are now persisted with IDs)
        branches.forEach { branch ->
            val persistedHead =
                commitsBySha[branch.head.sha]
                    ?: throw IllegalStateException("Head commit ${branch.head.sha} not found for branch ${branch.name}")

            // Create new branch entity pointing to persisted commit
            val newBranch = branch.copy(head = persistedHead)
            intermediateRepo.branches.add(newBranch)
        }

        val updated = repositoryDao.update(intermediateRepo)
        entityManager.flush()
        logger.trace("Phase 3: Branches persisted")

        return repositoryAssembler.refresh(value, updated)
    }

    @Transactional
    override fun saveAll(values: Collection<Repository>): Iterable<Repository> {
        // Create each repository (which modifies them in place)
        values.forEach { this.create(it) }
        // Return the original collection with updated values
        return values
    }

    @Transactional(readOnly = true)
    @MappingSession
    override fun findExistingCommits(
        repo: Repository,
        shas: Set<String>,
    ): Sequence<Commit> {
        val entity =
            repositoryDao.findByIid(repo.iid)
                ?: throw NotFoundException("Repository ${repo.uniqueKey} not found")
        logger.debug("Repository Entity found")
        ctx.remember(repo, entity)

        return this.commitDao
            .findExistingSha(repo, shas)
            .map { commitEntity ->
                commitMapper.toDomain(commitEntity)
            }.asSequence()
    }

    @Transactional(readOnly = true)
    @MappingSession
    override fun findBranch(
        repository: Repository,
        name: String,
    ): Branch? =
        this.repositoryDao.findByIid(repository.iid)?.let {
            this.branchDao.findByName(it, name)
            repositoryAssembler.toDomain(it).branches.find { branch -> branch.name == name }
        }
}
