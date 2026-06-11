package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.infrastructure.arangodb.assembler.ProjectAssembler
import com.inso_world.binocular.infrastructure.arangodb.assembler.RepositoryAssembler
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.ProjectDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.DeveloperMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.DeveloperRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.RepositoryRepository
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.toLegacyUser
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import kotlin.uuid.ExperimentalUuidApi

@Service
internal class ProjectInfrastructurePortImpl :
    AbstractInfrastructurePort<Project, String>(),
    ProjectInfrastructurePort {
    @PostConstruct
    fun init() {
        super.dao = projectDao
    }

    companion object {
        val logger by logger()
    }

    @Autowired
    private lateinit var projectDao: ProjectDao

    @Autowired
    private lateinit var projectMapper: ProjectMapper

    @Autowired
    private lateinit var repositoryMapper: RepositoryMapper

    @Autowired
    private lateinit var ctx: MappingContext

    @Autowired
    private lateinit var projectAssembler: ProjectAssembler

    @Autowired
    private lateinit var repositoryAssembler: RepositoryAssembler

    @Autowired
    @Lazy
    private lateinit var repositoryPort: RepositoryInfrastructurePortImpl

    @Autowired
    private lateinit var repositoryRepository: RepositoryRepository

    @Autowired
    private lateinit var developerRepository: DeveloperRepository

    @Autowired
    @Lazy
    private lateinit var developerMapper: DeveloperMapper

    @Autowired
    @Lazy
    private lateinit var commitPort: CommitInfrastructurePortImpl

    @Autowired
    @Lazy
    private lateinit var branchPort: BranchInfrastructurePortImpl

    @Autowired
    @Lazy
    private lateinit var userPort: UserInfrastructurePortImpl

    @Autowired
    @Lazy
    private lateinit var self: ProjectInfrastructurePortImpl

    @MappingSession
    override fun findAll(): Iterable<Project> {
        val p = this.projectDao.findAllEntities()
        return p.map(projectAssembler::toDomain)
    }

    @MappingSession
    override fun findAll(pageable: Pageable): Page<Project> = this.projectDao.findAll(pageable)

    @MappingSession
    override fun findById(id: String): Project? = this.projectDao.findById(id)

    /**
     * Persists a new [Project] including its owned [com.inso_world.binocular.model.Repository] child.
     *
     * The aggregate is assembled via [ProjectAssembler.toEntity] (not [ProjectMapper.toEntity], which
     * is structure-only and skips children) so that [ProjectEntity.repository][com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity.repository]
     * is wired and [ProjectDao.create]'s repository cascade persists the repository document and
     * updates the `@Ref`.
     *
     * The mapper's `toDomain` fast-path returns the cached pre-persistence domain object when
     * the entity's `iid` (a stable identity present before persistence) resolves to an existing
     * identity-map entry, which would leave [Project.id] `null` on the returned object. To avoid
     * that, the persisted entity is mapped back onto [value] in place via [ProjectMapper.refreshDomain]
     * (and the repository id via [RepositoryMapper.refreshDomain]) and [value] itself is returned
     * (mirroring `RepositoryInfrastructurePortImpl.update`).
     *
     * Repository children (developers, commits, branches) are persisted explicitly via
     * [persistRepositoryChildren] in `@Ref(lazy=false)` dependency order, since ArangoDB has no
     * cascade-persist. One legacy user document is also written per developer so that
     * [UserInfrastructurePort.findAll] reflects the developers, matching the SQL adapter.
     *
     * @param value the project to persist; must not yet have an [Project.id].
     * @return [value], refreshed with persisted values such as the generated [Project.id].
     */
    @MappingSession
    override fun create(value: Project): Project {
        logger.debug("Creating new project: {}", value)

        val toPersist = projectAssembler.toEntity(value)
        val persisted = projectDao.create(toPersist)

        projectMapper.refreshDomain(value, persisted)
        val repo = value.repo
        val repoEntity = persisted.repository
        if (repo != null && repoEntity != null) {
            repositoryMapper.refreshDomain(repo, repoEntity)
            persistRepositoryChildren(repo)
        }
        return value
    }

    @MappingSession
    override fun saveAll(values: Collection<Project>): Iterable<Project> = values.map { this.create(it) }

    @MappingSession
    override fun findByName(name: String): Project? = this.projectDao.findByName(name)

    @OptIn(ExperimentalUuidApi::class)
    @MappingSession
    override fun update(value: Project): Project {
        val managedEntity =
            this.projectDao.findEntityByIid(value.iid) ?: throw IllegalStateException("Project ${value.iid} not found")

        // update project properties
        managedEntity.description = value.description

        // Seed identity-map so child assemblers (e.g. RepositoryMapper) can locate this Project's entity
        ctx.remember(value, managedEntity)

        run {
            val domainRepo = value.repo
            val entityRepo = managedEntity.repository

            when {
                // Case 1: Both repos exist - check if they're the same and update
                domainRepo != null && entityRepo != null -> {
                    if (domainRepo.iid.value != entityRepo.iid) {
                        throw IllegalArgumentException(
                            "Cannot update project with a different repository. Project '$managedEntity' already has repository '${entityRepo.localPath}'",
                        )
                    }
                    // Seed identity-map so RepositoryMapper/Assembler short-circuit to the
                    // already-managed RepositoryEntity instead of constructing a new (id=null) one.
                    ctx.remember(domainRepo, entityRepo)

                    repositoryPort.update(domainRepo)
                }

                // Case 2: Adding a new repo where none existed
                domainRepo != null && entityRepo == null -> {
                    val repoEntity = repositoryAssembler.toEntity(domainRepo)
                    // ArangoDB has no cascade persist — explicitly save the repository entity.
                    repositoryRepository.save(repoEntity)
                    managedEntity.repository = repoEntity
                }

                // Case 3: Removing existing repo
                domainRepo == null && entityRepo != null -> {
                    throw UnsupportedOperationException("Deleting repository from project is not yet allowed")
                }

                // Case 4: No repo in either - nothing to do
                else -> {}
            }
        }

        // Persist the project entity (and cascade-save the repository if present)
        val updated = this.projectDao.create(managedEntity)

        // Refresh the input domain object with persisted values and return it
        this.projectMapper.refreshDomain(value, updated)
        return value
    }

    /**
     * Explicitly persists the repository's owned children, in `@Ref(lazy=false)` dependency order.
     *
     * ArangoDB has no cascade-persist, so [ProjectDao.create] only writes the project and
     * repository documents. This method saves the remaining child documents reusing the
     * per-type ports/mappers:
     * 1. **Developers** → `developers` collection (commit `author`/`committer` `@Ref` targets).
     * 2. **Commits** → `commits` (require their author/committer developers to already exist).
     * 3. **Branches** → `branches` (require their `head` commit to already exist).
     * 4. **Legacy users** → `users` collection, one per developer via [Developer.toLegacyUser],
     *    so [UserInfrastructurePort.findAll] (which reads `users`) reflects the developers —
     *    matching the SQL adapter where `DeveloperEntity` is the `users` table.
     *
     * All child mapper `toEntity` calls resolve their `@Ref`s from the current
     * [MappingContext], which [ProjectAssembler.toEntity] already populated; the saves run in
     * the [create] `@MappingSession`, so no new session is opened.
     *
     * @param repo the persisted repository whose children must be written.
     */
    private fun persistRepositoryChildren(repo: Repository) {
        repo.developers.forEach { developerRepository.save(developerMapper.toEntity(it)) }
        repo.commits.forEach { commitPort.create(it) }
        repo.branches.forEach { branchPort.create(it) }
        repo.developers.forEach { userPort.create(it.toLegacyUser()) }
    }

    /**
     * Finds a [Project] by its [Project.Id].
     *
     * Delegates to [findByIidInternal] via the self-injected proxy ([self]) so that the
     * `@MappingSession` AOP advice on the internal method actually applies. A direct
     * `@MappingSession` annotation on this method would not work because [Project.Id] is a
     * Kotlin value class, which causes JVM signature mangling and breaks Spring AOP's
     * `@annotation` pointcut matching (see `core/CLAUDE.md` invariant on value-class self-injection).
     *
     * @param iid the project's stable identity.
     * @return the matching [Project], or `null` if none exists.
     */
    override fun findByIid(iid: Project.Id): Project? = self.findByIidInternal(iid)

    /**
     * Internal implementation of [findByIid], opening a [MappingSession] so that
     * [ProjectMapper] can resolve cached entities/domains during the lookup.
     *
     * @param iid the project's stable identity.
     * @return the matching [Project], or `null` if none exists.
     */
    @MappingSession
    protected fun findByIidInternal(iid: Project.Id): Project? = this.projectDao.findByIid(iid)
}
