package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.arangodb.assembler.RepositoryAssembler
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IBranchDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IRepositoryDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.CommitDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.RepositoryDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.BranchMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.DeveloperMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.UserMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.BranchRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.CommitRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.DeveloperRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.RepositoryRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.UserRepository
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
internal class RepositoryInfrastructurePortImpl(
    private val branchDao: IBranchDao,
) : AbstractInfrastructurePort<Repository, String>(),
    RepositoryInfrastructurePort {
    @PostConstruct
    fun init() {
        super.dao = repositoryDao
    }

    companion object {
        val logger by logger()
    }

    @Autowired
    private lateinit var commitDao: CommitDao

    @Autowired
    private lateinit var repositoryDao: IRepositoryDao

    @Autowired
    private lateinit var repositoryDaoImpl: RepositoryDao

    @Autowired
    private lateinit var repositoryMapper: RepositoryMapper

    @Autowired
    @Lazy
    private lateinit var repositoryAssembler: RepositoryAssembler

    @Autowired
    private lateinit var developerRepository: DeveloperRepository

    @Autowired
    private lateinit var commitRepository: CommitRepository

    @Autowired
    private lateinit var branchRepository: BranchRepository

    @Autowired
    private lateinit var repositoryRepository: RepositoryRepository

    @Autowired
    private lateinit var projectMapper: ProjectMapper

    @Autowired
    private lateinit var ctx: MappingContext

    @Autowired
    private lateinit var developerMapper: DeveloperMapper

    @Autowired
    private lateinit var commitMapper: CommitMapper

    @Autowired
    private lateinit var branchMapper: BranchMapper

    @Autowired
    private lateinit var userMapper: UserMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @MappingSession
    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    override fun findByIid(iid: Repository.Id): Repository? =
        this.repositoryDao.findByIid(iid.value)?.let { repo ->
            this.repositoryMapper.toDomain(repo)
        }

    @MappingSession
    override fun findAll(): Iterable<Repository> = this.repositoryDao.findAll()

    @MappingSession
    override fun findAll(pageable: Pageable): Page<Repository> = this.repositoryDao.findAll(pageable)

    @MappingSession
    override fun findById(id: String): Repository? = this.repositoryDao.findById(id)

    @MappingSession
    override fun create(value: Repository): Repository {
        // Ensure parent references are in context via assembler
        repositoryAssembler.toEntity(value)
        // Pass domain object to DAO (DAO handles conversion internally)
        return repositoryDao.create(value)
    }

    @MappingSession
    override fun saveAll(values: Collection<Repository>): Iterable<Repository> {
        values.forEach { create(it) }
        return values
    }

    /**
     * Persists changes to an existing [Repository] aggregate, cascading to its owned children.
     *
     * ### Semantics
     * Loads the persisted [RepositoryEntity] by [Repository.iid], reuses it as the mapping target
     * (ArangoDB has no cascade-persist), and explicitly saves each child collection:
     * 1. **Developers** → `developers` (commits reference them via [Signature]).
     * 2. **Commits** → `commits`.
     * 3. **Branches** → `branches`.
     * 4. **Legacy `User`s** → `users`, refreshing each `User.id` from the saved
     *    `UserEntity` so callers observe a non-null id afterwards (contract: `UserTest`).
     * 5. The repository document itself, in case its own fields changed.
     *
     * ### Invariants & requirements
     * - [value.project][Repository.project] and `value` itself are seeded into the
     *   [MappingContext] before any child mapper runs, so [UserMapper.toEntity] and the other
     *   child mappers can resolve their owning [RepositoryEntity]/`ProjectEntity` `@Ref`s.
     * - Runs inside a single [MappingSession]; child saves do not open new sessions.
     *
     * @param value the repository (with its in-memory children) to persist.
     * @return the same [Repository] instance, refreshed with generated ids.
     * @throws IllegalStateException if no [RepositoryEntity] exists for `value.iid`.
     */
    @MappingSession
    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    override fun update(value: Repository): Repository {
        // Load the existing repository entity from DB — has the correct ID so @Ref resolution works.
        val existingEntity =
            this.repositoryDao.findEntityByIid(value.iid.value)
                ?: throw IllegalStateException("Repository ${value.iid} not found")

        // Seed identity map so children's mappers reuse known entities.
        // Note: RepositoryEntity.project is @Ref(lazy = true), so we load project from domain.
        val projectEntity =
            ctx.findEntity<Project.Key, Project, ProjectEntity>(value.project)
                ?: projectMapper.toEntity(value.project)
        ctx.remember(value.project, projectEntity)
        ctx.remember(value, existingEntity)

        // Ensure the project reference is set on the existing entity (it's lazy-loaded and may be null)
        existingEntity.project = projectEntity

        // Save children. Order matters because commits reference developers via Signature.
        value.developers.forEach { dev ->
            val devEntity = developerMapper.toEntity(dev)
            devEntity.repository = existingEntity
            developerRepository.save(devEntity)
        }
        value.commits.forEach { commit ->
            val commitEntity = commitMapper.toEntity(commit)
            commitRepository.save(commitEntity)
        }
        value.branches.forEach { branch ->
            val branchEntity = branchMapper.toEntity(branch)
            branchRepository.save(branchEntity)
        }

        // Legacy User children: persist and write generated ids back so callers
        // observe non-null User.id after update (contract: UserTest).
        value.user.forEach { user ->
            val userEntity = userMapper.toEntity(user)
            userEntity.repository = existingEntity
            val savedUser = userRepository.save(userEntity)
            userMapper.refreshDomain(user, savedUser)
        }

        // Persist repository entity itself in case its fields changed
        val saved = repositoryRepository.save(existingEntity)
        repositoryMapper.refreshDomain(value, saved)
        return value
    }

    @MappingSession
    override fun findByName(name: String): Repository? = this.repositoryDao.findByName(name)?.let { this.repositoryMapper.toDomain(it) }

    @MappingSession
    override fun findExistingCommits(
        repo: Repository,
        shas: Set<String>,
    ): Sequence<Commit> = commitDao.findByRepositoryAndShaIn(repo.localPath, shas).asSequence()

    @MappingSession
    override fun findBranch(
        repository: Repository,
        name: String,
    ): Branch? = branchDao.findByRepositoryAndName(repository.localPath, name)
}
