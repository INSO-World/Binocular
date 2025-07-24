package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.exception.BinocularInfrastructureException
import com.inso_world.binocular.core.persistence.exception.PersistenceException
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.exception.NotFoundException
import com.inso_world.binocular.infrastructure.sql.mapper.BranchMapper
import com.inso_world.binocular.infrastructure.sql.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.sql.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.sql.mapper.UserMapper
import com.inso_world.binocular.infrastructure.sql.persistence.dao.BranchDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.CommitDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.UserDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IRepositoryDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import com.inso_world.binocular.model.Branch
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
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

@Service
@Validated
internal class CommitInfrastructurePortImpl(
    @Lazy @Autowired private val repositoryDao: IRepositoryDao,
    @Autowired private val commitDao: CommitDao,
    @Lazy @Autowired private val branchDao: BranchDao,
    @Lazy @Autowired private val userDao: UserDao,
    @Autowired private val commitMapper: CommitMapper,
) : AbstractInfrastructurePort<Commit, CommitEntity, Long>(Long::class),
    CommitInfrastructurePort {
    var logger: Logger = LoggerFactory.getLogger(CommitInfrastructurePort::class.java)

    @Autowired
    @Lazy
    private lateinit var projectMapper: ProjectMapper

    @Autowired
    @Lazy
    private lateinit var branchMapper: BranchMapper

    @Autowired
    @Lazy
    private lateinit var repositoryMapper: RepositoryMapper

    @Autowired
    @Lazy
    private lateinit var userMapper: UserMapper

    @PostConstruct
    fun init() {
        super.dao = commitDao
//        super.mapper = commitMapper
    }

    override fun create(value: Commit): Commit {
        val repositoryId =
            value.repositoryId?.toLong() ?: throw IllegalArgumentException("repositoryId of Commit must not be null")
        val repository =
            repositoryDao.findById(repositoryId)
                ?: throw NotFoundException("Repository id=${value.repositoryId} not found")

        val mapped =
            run {
                val commitContext = repository.commits.associateBy { it.sha }.toMutableMap()
                val branchContext =
                    repository.branches
                        .associateBy {
                            it.uniqueKey()
                        }.toMutableMap()
                val userContext =
                    repository.user.associateBy(UserEntity::uniqueKey).toMutableMap()

                commitMapper.toEntity(
                    value,
                    repository,
                    commitContext,
                    branchContext,
                    userContext,
                )
            }
        return this.commitDao.create(mapped).let {
            commitDao.flush()
            val commitContext: MutableMap<String, Commit> = mutableMapOf()
            val branchContext: MutableMap<String, Branch> = mutableMapOf()
            val userContext: MutableMap<String, User> = mutableMapOf()

            val project =
                projectMapper.toDomain(
                    repository.project,
                    commitContext,
                    branchContext,
                    userContext,
                )

            val repoModel =
                repositoryMapper.toDomain(
                    it.repository ?: throw NotFoundException("Repository must not be null after saving commit"),
                    project,
                    commitContext,
                    branchContext,
                    userContext,
                )
            return@let run {
                val domain = commitMapper.toDomain(it, repoModel, commitContext, branchContext, userContext)
//                val violations = validator.validate(domain, FromInfrastructure::class.java)
//                if (violations.isNotEmpty()) {
//                    throw ConstraintViolationException(violations)
//                }
                domain
            }
        }
    }

    override fun findAll(): Iterable<Commit> {
        val repoContext: MutableMap<Long, Repository> = mutableMapOf()
        val commitContext: MutableMap<String, Commit> = mutableMapOf()
        val branchContext: MutableMap<String, Branch> = mutableMapOf()
        val userContext: MutableMap<String, User> = mutableMapOf()

        return this.commitDao.findAll().map { c ->
            val repoEntity = c.repository ?: throw IllegalStateException("Repository of a Commit cannot be null")
            if (repoEntity.id == null) {
                throw IllegalStateException("Id of an existing repo cannot be null")
            }
            val repo =
                repoContext.getOrPut(repoEntity.id) {
                    val project =
                        projectMapper.toDomain(
                            repoEntity.project,
                            commitContext,
                            branchContext,
                            userContext,
                        )

                    val repo = this.repositoryMapper.toDomain(repoEntity, project, commitContext, branchContext, userContext)

                    commitContext.putAll(
                        repo.commits.associateBy { it.sha },
                    )
                    branchContext.putAll(
                        repo.branches.associateBy { "${repo.name},${it.name}" },
                    )
                    userContext.putAll(
                        repo.user.associateBy(User::uniqueKey),
                    )
                    return@getOrPut repo
                }

            this.commitMapper.toDomain(c, repo, commitContext, branchContext, userContext)
        }
    }

    override fun findAll(pageable: Pageable): Page<Commit> {
        TODO("Not yet implemented")
    }

    override fun findById(id: String): Commit? =
        this.commitDao.findById(id.toLong())?.let {
            val commitContext = mutableMapOf<String, Commit>()
            val branchContext = mutableMapOf<String, Branch>()
            val userContext = mutableMapOf<String, User>()

            val repository =
                it.repository?.let { r ->
                    val project =
                        projectMapper.toDomain(
                            r.project,
                            commitContext,
                            branchContext,
                            userContext,
                        )

                    repositoryMapper.toDomain(r, project, commitContext, branchContext, userContext)
                }
            if (repository == null) {
                throw IllegalStateException("Repository cannot be null when finding commit by ID")
            }

            commitMapper.toDomain(it, repository, commitContext, branchContext, userContext)
        }

    override fun update(value: Commit): Commit {
        val repositoryId =
            value.repositoryId?.toLong() ?: throw IllegalArgumentException("projectId of Repository must not be null")
        val repository =
            repositoryDao.findById(repositoryId)
                ?: throw NotFoundException("Repository ${value.repositoryId} not found")

        val entity =
            this.commitDao.findBySha(repository, value.sha)
                ?: throw NotFoundException("Commit ${value.sha} not found, required for update")

//        entity.authorDateTime = value.authorDateTime
//        entity.commitDateTime = value.commitDateTime
        entity.message = value.message
        entity.webUrl = value.webUrl

        repository.branches.size // Force initialization

        val commitContext: MutableMap<String, CommitEntity> = repository.commits.associateBy { it.sha }.toMutableMap()
        val branchContext: MutableMap<String, BranchEntity> =
            repository.branches
                .associateBy(BranchEntity::uniqueKey)
                .toMutableMap()
        val userContext: MutableMap<String, UserEntity> =
            repository.user
                .associateBy(UserEntity::uniqueKey)
                .toMutableMap()

        entity.branches.addAll(
            value.branches.map {
                val existing =
                    branchContext["${repository.name},${it.name}"]
                if (existing == null) {
//                    create new branch if not exists
                    val newBranch =
                        branchMapper.toEntity(
                            it,
                            repository,
                            commitContext,
                            branchContext,
                        )
                    return@map branchDao.create(newBranch)
                }
                return@map existing
            },
        )
        entity.committer =
            value.committer?.let { user ->
                val existing = userContext["${repository.name},${user.name}"]
                if (existing == null) {
                    val newUser =
                        userMapper.toEntity(
                            user,
                            repository,
                            commitContext,
                            userContext,
                        )
                    return@let userDao.create(newUser)
                }
                return@let existing
            }
        entity.author =
            value.author?.let { user ->
                val existing = userContext["${repository.name},${user.name}"]
                if (existing == null) {
                    val newUser =
                        userMapper.toEntity(
                            user,
                            repository,
                            commitContext,
                            userContext,
                        )
                    return@let userDao.create(newUser)
                }
                return@let existing
            }

        entity.branches.removeAll { !value.branches.map { b -> b.name }.contains(it.name) }
        repository.branches.removeAll { !value.branches.map { b -> b.name }.contains(it.name) }

        return this.commitDao
            .update(entity)
            .let {
                this.commitDao.flush()
                val commitContext = mutableMapOf<String, Commit>()
                val branchContext = mutableMapOf<String, Branch>()
                val userContext = mutableMapOf<String, User>()

                val project =
                    projectMapper.toDomain(
                        repository.project,
                        commitContext,
                        branchContext,
                        userContext,
                    )

                val repository = repositoryMapper.toDomain(repository, project, commitContext, branchContext, userContext)
                commitContext.putAll(repository.commits.associateBy { c -> c.sha })
                branchContext.putAll(
                    repository.branches.associateBy { b -> "${repository.name},${b.name}" },
                )
                userContext.putAll(repository.user.associateBy { u -> "${repository.name},${u.name}" })
                commitMapper.toDomain(it, repository, commitContext, branchContext, userContext)
            }
    }

    override fun updateAndFlush(value: Commit): Commit {
        val updated = update(value)
        this.commitDao.flush()
        return updated
    }

    override fun saveAll(values: Collection<Commit>): Iterable<Commit> {
        TODO("Not yet implemented")
    }

    override fun delete(value: Commit) {
        val repositoryId =
            value.repositoryId?.toLong() ?: throw IllegalArgumentException("projectId of Repository must not be null")
        val repository =
            repositoryDao.findById(repositoryId)
                ?: throw NotFoundException("Repository ${value.repositoryId} not found")

        val mapped = commitMapper.toEntity(value, repository, mutableMapOf(), mutableMapOf(), mutableMapOf())
        this.commitDao.delete(mapped)
    }

    override fun deleteById(id: String) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        this.commitDao.deleteAll()
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

    @Transactional(readOnly = true)
    override fun findExistingSha(
        repo: Repository,
        shas: List<String>,
    ): Iterable<Commit> {
        val repoEntity =
            repositoryDao.findByName(repo.name) ?: throw NotFoundException("Repository ${repo.name} not found")

        val commitContext = mutableMapOf<String, Commit>()
        val branchContext = mutableMapOf<String, Branch>()
        val userContext = mutableMapOf<String, User>()

        val project =
            projectMapper.toDomain(
                repoEntity.project,
                commitContext,
                branchContext,
                userContext,
            )

        val repoModel = repositoryMapper.toDomain(repoEntity, project, commitContext, branchContext, userContext)

        return this.commitDao
            .findExistingSha(repoEntity, shas)
            .map {
                this.commitMapper.toDomain(it, repoModel, commitContext, branchContext, userContext)
            }
    }

    override fun findAllByRepo(
        repo: Repository,
        pageable: Pageable,
    ): Iterable<Commit> {
        val repoEntity =
            repositoryDao.findByName(repo.name) ?: throw NotFoundException("Repository ${repo.name} not found")

        val commitContext = mutableMapOf<String, Commit>()
        val branchContext = mutableMapOf<String, Branch>()
        val userContext = mutableMapOf<String, User>()

        val project =
            projectMapper.toDomain(
                repoEntity.project,
                commitContext,
                branchContext,
                userContext,
            )

        val repoModel = repositoryMapper.toDomain(repoEntity, project, commitContext, branchContext, userContext)
        return try {
            this.commitDao
                .findAllByRepo(
                    repoEntity,
                    pageable,
                ).map { this.commitMapper.toDomain(it, repoModel, commitContext, branchContext, userContext) }
        } catch (e: PersistenceException) {
            throw BinocularInfrastructureException(e)
        }
    }

    override fun findAll(repo: Repository): Iterable<Commit> {
        val commitContext = mutableMapOf<String, Commit>()
        val branchContext = mutableMapOf<String, Branch>()
        val userContext = mutableMapOf<String, User>()

        return this.commitDao
            .findAll(repo)
            .map { this.commitMapper.toDomain(it, repo, commitContext, branchContext, userContext) }
    }

    override fun findHeadForBranch(
        repo: Repository,
        branch: String,
    ): Commit? {
        val repoEntity =
            repositoryDao.findByName(repo.name) ?: throw NotFoundException("Repository ${repo.name} not found")

        val commitContext = mutableMapOf<String, Commit>()
        val branchContext = mutableMapOf<String, Branch>()
        val userContext = mutableMapOf<String, User>()

        val project =
            projectMapper.toDomain(
                repoEntity.project,
                commitContext,
                branchContext,
                userContext,
            )

        val repoModel = repositoryMapper.toDomain(repoEntity, project, commitContext, branchContext, userContext)
        return this.commitDao
            .findHeadForBranch(
                repoEntity,
                branch,
            )?.let { this.commitMapper.toDomain(it, repoModel, commitContext, branchContext, userContext) }
    }

    override fun findAllLeafCommits(repo: Repository): Iterable<Commit> {
        val repoEntity =
            repositoryDao.findByName(repo.name) ?: throw NotFoundException("Repository ${repo.name} not found")

        val commitContext = mutableMapOf<String, Commit>()
        val branchContext = mutableMapOf<String, Branch>()
        val userContext = mutableMapOf<String, User>()

        val project =
            projectMapper.toDomain(
                repoEntity.project,
                commitContext,
                branchContext,
                userContext,
            )

        val repoModel = repositoryMapper.toDomain(repoEntity, project, commitContext, branchContext, userContext)
        return this.commitDao
            .findAllLeafCommits(
                repoEntity,
            ).map { this.commitMapper.toDomain(it, repoModel, commitContext, branchContext, userContext) }
    }
}
