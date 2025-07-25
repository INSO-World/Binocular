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
import com.inso_world.binocular.infrastructure.sql.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.sql.mapper.context.MappingSession
import com.inso_world.binocular.infrastructure.sql.persistence.dao.BranchDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.CommitDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.RepositoryDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.UserDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
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
internal class CommitInfrastructurePortImpl
    @Autowired
    constructor(
        private val commitMapper: CommitMapper,
        private val commitDao: CommitDao,
        @Lazy private val branchDao: BranchDao,
        @Lazy private val userDao: UserDao,
        @Lazy private val repositoryDao: RepositoryDao,
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
        private lateinit var ctx: MappingContext

        @Autowired
        @Lazy
        private lateinit var userMapper: UserMapper

        @PostConstruct
        fun init() {
            super.dao = commitDao
//        super.mapper = commitMapper
        }

        @MappingSession
        override fun create(value: Commit): Commit {
            val repositoryId =
                value.repositoryId?.toLong() ?: throw IllegalArgumentException("repositoryId of Commit must not be null")
            val repository =
                repositoryDao.findById(repositoryId)
                    ?: throw NotFoundException("Repository id=${value.repositoryId} not found")

            val mapped =
                run {
//                TODO N+1 here
                    ctx.entity.commit.putAll(repository.commits.associateBy { it.sha })
                    ctx.entity.branch.putAll(
                        repository.branches
                            .associateBy {
                                it.uniqueKey()
                            },
                    )
                    ctx.entity.user.putAll(repository.user.associateBy(UserEntity::uniqueKey))

                    commitMapper.toEntity(
                        value,
                        repository,
                    )
                }
            return this.commitDao.create(mapped).let {
                commitDao.flush()
                val project =
                    projectMapper.toDomain(
                        repository.project,
                    )

                val repoModel =
                    repositoryMapper.toDomain(
                        it.repository ?: throw NotFoundException("Repository must not be null after saving commit"),
                        project,
                    )
                return@let run {
                    val domain = commitMapper.toDomain(it, repoModel)
//                val violations = validator.validate(domain, FromInfrastructure::class.java)
//                if (violations.isNotEmpty()) {
//                    throw ConstraintViolationException(violations)
//                }
                    domain
                }
            }
        }

        @MappingSession
        override fun findAll(): Iterable<Commit> {
            val repoContext: MutableMap<Long, Repository> = mutableMapOf()

            return this.commitDao.findAll().map { c ->
                val repoEntity = c.repository ?: throw IllegalStateException("Repository of a Commit cannot be null")
                if (repoEntity.id == null) {
                    throw IllegalStateException("Id of an existing repo cannot be null")
                }
                val repo =
                    repoContext.computeIfAbsent(repoEntity.id) {
                        val project =
                            projectMapper.toDomain(
                                repoEntity.project,
                            )

                        val repo =
                            this.repositoryMapper.toDomain(repoEntity, project)

                        ctx.domain.commit.putAll(
                            repo.commits.associateBy { it.sha },
                        )
                        ctx.domain.branch.putAll(
                            repo.branches.associateBy { "${repo.name},${it.name}" },
                        )
                        ctx.domain.user.putAll(
                            repo.user.associateBy(User::uniqueKey),
                        )
                        return@computeIfAbsent repo
                    }

                this.commitMapper.toDomain(c, repo)
            }
        }

        override fun findAll(pageable: Pageable): Page<Commit> {
            TODO("Not yet implemented")
        }

        @MappingSession
        override fun findById(id: String): Commit? =
            this.commitDao.findById(id.toLong())?.let {
                val repository =
                    it.repository?.let { r ->
                        val project =
                            projectMapper.toDomain(
                                r.project,
                            )

                        repositoryMapper.toDomain(r, project)
                    }
                if (repository == null) {
                    throw IllegalStateException("Repository cannot be null when finding commit by ID")
                }

                commitMapper.toDomain(it, repository)
            }

        @MappingSession
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

//        TODO N+1 here
            ctx.entity.commit.putAll(repository.commits.associateBy(CommitEntity::uniqueKey))
            ctx.entity.branch.putAll(repository.branches.associateBy(BranchEntity::uniqueKey))
            ctx.entity.user.putAll(repository.user.associateBy(UserEntity::uniqueKey))

            entity.branches.addAll(
                value.branches.map {
                    val existing =
                        ctx.entity.branch["${repository.name},${it.name}"]
                    if (existing == null) {
//                    create new branch if not exists
                        val newBranch =
                            branchMapper.toEntity(
                                it,
                                repository,
                            )
                        return@map branchDao.create(newBranch)
                    }
                    return@map existing
                },
            )
            entity.committer =
                value.committer?.let { user ->
                    val existing = ctx.entity.user["${repository.name},${user.name}"]
                    if (existing == null) {
                        val newUser =
                            userMapper.toEntity(
                                user,
                                repository,
                            )
                        return@let userDao.create(newUser)
                    }
                    return@let existing
                }
            entity.author =
                value.author?.let { user ->
                    val existing = ctx.entity.user["${repository.name},${user.name}"]
                    if (existing == null) {
                        val newUser =
                            userMapper.toEntity(
                                user,
                                repository,
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
                    val project =
                        projectMapper.toDomain(
                            repository.project,
                        )

                    val repository =
                        repositoryMapper.toDomain(repository, project)
                    ctx.domain.commit.putAll(repository.commits.associateBy { c -> c.sha })
                    ctx.domain.branch.putAll(
                        repository.branches.associateBy { b -> "${repository.name},${b.name}" },
                    )
                    ctx.domain.user.putAll(repository.user.associateBy { u -> "${repository.name},${u.name}" })
                    commitMapper.toDomain(it, repository)
                }
        }

//        @MappingSession
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

            val mapped = commitMapper.toEntity(value, repository)
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
        @MappingSession
        override fun findExistingSha(
            repo: Repository,
            shas: List<String>,
        ): Iterable<Commit> {
            val repoEntity =
                repositoryDao.findByName(repo.name) ?: throw NotFoundException("Repository ${repo.name} not found")

            val project =
                projectMapper.toDomain(
                    repoEntity.project,
                )

            val repoModel = repositoryMapper.toDomain(repoEntity, project)

            return this.commitDao
                .findExistingSha(repoEntity, shas)
                .map {
                    this.commitMapper.toDomain(it, repoModel)
                }
        }

        @MappingSession
        override fun findAllByRepo(
            repo: Repository,
            pageable: Pageable,
        ): Iterable<Commit> {
            val repoEntity =
                repositoryDao.findByName(repo.name) ?: throw NotFoundException("Repository ${repo.name} not found")

            val project =
                projectMapper.toDomain(
                    repoEntity.project,
                )

            val repoModel = repositoryMapper.toDomain(repoEntity, project)
            return try {
                this.commitDao
                    .findAllByRepo(
                        repoEntity,
                        pageable,
                    ).map { this.commitMapper.toDomain(it, repoModel) }
            } catch (e: PersistenceException) {
                throw BinocularInfrastructureException(e)
            }
        }

        @MappingSession
        override fun findAll(repo: Repository): Iterable<Commit> =
            this.commitDao
                .findAll(repo)
                .map { this.commitMapper.toDomain(it, repo) }

        @MappingSession
        override fun findHeadForBranch(
            repo: Repository,
            branch: String,
        ): Commit? {
            val repoEntity =
                repositoryDao.findByName(repo.name) ?: throw NotFoundException("Repository ${repo.name} not found")

            val project =
                projectMapper.toDomain(
                    repoEntity.project,
                )

            val repoModel = repositoryMapper.toDomain(repoEntity, project)
            return this.commitDao
                .findHeadForBranch(
                    repoEntity,
                    branch,
                )?.let { this.commitMapper.toDomain(it, repoModel) }
        }

        @MappingSession
        override fun findAllLeafCommits(repo: Repository): Iterable<Commit> {
            val repoEntity =
                repositoryDao.findByName(repo.name) ?: throw NotFoundException("Repository ${repo.name} not found")

            val project =
                projectMapper.toDomain(
                    repoEntity.project,
                )

            val repoModel = repositoryMapper.toDomain(repoEntity, project)
            return this.commitDao
                .findAllLeafCommits(
                    repoEntity,
                ).map { this.commitMapper.toDomain(it, repoModel) }
        }
    }
