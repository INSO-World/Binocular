package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.exception.NotFoundException
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.assembler.RepositoryAssembler
import com.inso_world.binocular.infrastructure.sql.mapper.BranchMapper
import com.inso_world.binocular.infrastructure.sql.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.sql.mapper.UserMapper
import com.inso_world.binocular.infrastructure.sql.persistence.dao.BranchDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.CommitDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.ProjectDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.RepositoryDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.service.AggregateFetchSupport.loadRepositoryEntities
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.CommitDiff
import com.inso_world.binocular.model.Repository
import jakarta.annotation.PostConstruct
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
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
    private lateinit var userMapper: UserMapper

    @Autowired
    private lateinit var branchDao: BranchDao

    @Autowired
    private lateinit var ctx: MappingContext

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    @Autowired
    @Lazy
    lateinit var commitMapper: CommitMapper

    @Autowired
    @Lazy
    private lateinit var branchMapper: BranchMapper

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

    @PostConstruct
    fun init() {
        super.dao = repositoryDao
//        super.mapper = repositoryMapper
    }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findByName(name: String): Repository? =
        this.repositoryDao.findByName(name)?.let {
            this.repositoryAssembler.toDomain(it)
        }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findAll(): Iterable<Repository> =
        loadRepositoryEntities(projectDao).map(repositoryAssembler::toDomain)

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
    override fun findByIid(iid: Repository.Id): Repository? {
        return self.findByIidInternal(iid)
    }

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
    protected fun findByIidInternal(iid: Repository.Id): Repository? {
        return this.repositoryDao.findByIid(iid)?.let {
            repositoryAssembler.toDomain(it)
        }
    }

    @MappingSession
    @Transactional
    override fun create(@Valid value: Repository): Repository {
        val project =
            projectDao.findByIid(value.project.iid) ?: throw NotFoundException("Project ${value.project} not found")

        if (project.repo != null) {
            throw IllegalArgumentException("Selected project $project has already a Repository set")
        }

        val toPersist = this.repositoryAssembler.toEntity(value)
        val persisted = super.create(toPersist)

        // Refresh the input domain object with persisted values and return it
        this.repositoryMapper.refreshDomain(value, persisted)
        return value
    }


    @MappingSession
    @Transactional
    override fun update(value: Repository): Repository {
        val entity =
            repositoryDao.findByIid(value.iid)
                ?: throw NotFoundException("Repository ${value.uniqueKey} not found")
        logger.debug("Repository Entity found")
        ctx.remember(value, entity)

        // Phase 0: Map existing entities to context (critical for idempotency!)
        // This prevents creating duplicate entities for existing commits/users/branches
        logger.trace("Mapping existing entities to context")

        // Map existing users first (commits reference users)
        entity.user.forEach { userEntity ->
            val domainUser = value.user.find { it.email == userEntity.email && it.name == userEntity.name }
            if (domainUser != null) {
                ctx.remember(domainUser, userEntity)
            }
        }

        // Map existing commits
        entity.commits.forEach { commitEntity ->
            val domainCommit = value.commits.find { it.sha == commitEntity.sha }
            if (domainCommit != null) {
                ctx.remember(domainCommit, commitEntity)
            }
        }

        // Map existing branches
        entity.branches.forEach { branchEntity ->
            val domainBranch = value.branches.find { it.name == branchEntity.name }
            if (domainBranch != null) {
                ctx.remember(domainBranch, branchEntity)
            }
        }

        // Phase 1: Map and wire Commits with their author/committer relationships
        // Collect all commits including parents and children to ensure complete graph
        logger.debug("Update commits")
        val allCommits =
            (value.commits + value.commits.flatMap { it.parents } + value.commits.flatMap { it.children }).toSet()

        allCommits.forEach { commit ->
            // Map commit entity (or get existing from context)
            val commitEntity = commitMapper.toEntity(commit)

            // Only set relationships if not already set (for idempotency)
            // Note: CommitEntity init block automatically adds entity to repository.commits
            if (commitEntity.committer == null) {
                // Wire author relationship if present
                commit.author?.let { author ->
                    val authorEntity = userMapper.toEntity(author)
                    commitEntity.author = authorEntity
                    entity.user.add(authorEntity)
                }

                // Wire committer relationship (required)
                val committerEntity = userMapper.toEntity(commit.committer)
                commitEntity.committer = committerEntity
                entity.user.add(committerEntity)
            }
        }

        // Add or update branches
        logger.debug("Update branches")
        value.branches.forEach { branch ->
            val branchEntity = branchMapper.toEntity(branch)
            // Only add if not already present (idempotency)
            if (!entity.branches.contains(branchEntity)) {
                entity.branches.add(branchEntity)
            }
        }

        logger.trace("Branches updated")

        val updated = repositoryDao.update(entity)

        logger.trace("Update executed")
        return repositoryMapper.refreshDomain(value, updated)
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
    override fun findExistingCommits(repo: Repository, shas: Set<String>): Sequence<Commit> {
        TODO("Not yet implemented")
    }

    @MappingSession
    override fun saveCommitDiffs(
        repository: Repository,
        diffs: Set<CommitDiff>,
    ): Set<CommitDiff> {
        TODO("Not yet implemented")
//                val savedDiffs = commitDiffService.saveCommitDiffs(repoEntity, diffs)
//        return savedDiffs
//            .map {
//                val domain = commitDiffMapper.toDomain(it)
//
//                val fileDiffs = it.files.map { fd -> fileDiffMapper.toDomain(fd) }
//                domain.files = fileDiffs.toSet()
//
//                return@map domain
//            }.map {
//                it.repository = repository
//                it
//            }.toSet()
    }

    @MappingSession
    override fun findAllDiffs(repository: Repository): Set<CommitDiff> {
        TODO("Not yet implemented")
//        val diffs = commitDiffDao.findAll()
//        val value =
//            (diffs.map { it.source } + diffs.mapNotNull { it.target }).toSet()
//        commitMapper.toDomainGraph(value.asSequence())
//
//        return diffs
//            .map {
//                val domain = commitDiffMapper.toDomain(it)
//
//                val fileDiffs = it.files.map { fd -> fileDiffMapper.toDomain(fd) }
//                domain.files = fileDiffs.toSet()
//
//                return@map domain
//            }.map {
//                it.repository = repository
//                it
//            }.toSet()
    }
    //    override fun findAllUser(repository: Repository): Iterable<User> {
//        val commitContext = mutableMapOf<String, Commit>()
//        val branchContext = mutableMapOf<String, Branch>()
//        val userContext = mutableMapOf<String, User>()
//
//        val entities = this.repositoryDao.findAllUser(repository.name)
//
//        return entities.map {
//            userMapper.toDomain(it, repository, userContext, commitContext, branchContext)
//        }
//    }
//
//    override fun findAllCommits(repository: Repository): Iterable<Commit> {
//        val commitContext = mutableMapOf<String, Commit>()
//        val branchContext = mutableMapOf<String, Branch>()
//        val userContext = mutableMapOf<String, User>()
//
//        val entities = this.repositoryDao.findAllCommits(repository.name)
//
//        return entities.map {
//            commitMapper.toDomain(it, repository, commitContext, branchContext, userContext)
//        }
//    }
//
//    override fun findAllBranches(repository: Repository): Iterable<Branch> {
//        val commitContext = mutableMapOf<String, Commit>()
//        val branchContext = mutableMapOf<String, Branch>()
//
//        val entities = this.repositoryDao.findAllBranches(repository.name)
//
//        return entities.map {
//            branchMapper.toDomain(it, repository, commitContext, branchContext)
//        }
//    }
}
