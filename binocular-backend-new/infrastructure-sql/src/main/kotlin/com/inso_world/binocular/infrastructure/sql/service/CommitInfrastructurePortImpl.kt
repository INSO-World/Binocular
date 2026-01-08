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
import com.inso_world.binocular.model.FileOwnership
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Module
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Stats
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
import java.util.stream.Collectors

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
        }

        @MappingSession
        @Transactional
        override fun create(value: Commit): Commit {
            val mapped =
                run {
                    val repositoryId =
                        value.repository?.id?.toLong()
                            ?: throw IllegalArgumentException("repository.id of Commit must not be null")
                    val repository =
                        repositoryDao.findById(repositoryId)
                            ?: throw NotFoundException("Repository id=${value.repository?.id} not found")

//                TODO N+1 here
                    ctx.entity.commit.putAll(repository.commits.associateBy(CommitEntity::uniqueKey))
                    ctx.entity.branch.putAll(repository.branches.associateBy(BranchEntity::uniqueKey))
                    ctx.entity.user.putAll(repository.user.associateBy(UserEntity::uniqueKey))

                    val mappedCommit = commitMapper.toEntityFull(value, repository)
                    return@run mappedCommit
                }
            return this.commitDao
                .create(mapped)
                .also { commitDao.flush() }
                .let { commitEntity ->
                    val project =
                        projectMapper.toDomain(
                            mapped.repository?.project
                                ?: throw IllegalStateException("Project disappeared after creating commit"),
                        )

                    val repoModel =
                        project.repo ?: throw IllegalStateException("Repository disappeared after creating commit")
                    repoModel.commits.find { it.sha == commitEntity.sha }
                        ?: throw IllegalStateException("Commit disappeared from repository after creating")
                }
        }

        @MappingSession
        @Transactional(readOnly = true)
        override fun findAll(): Iterable<Commit> {
            val values = this.commitDao.findAll()
            return values
                .associateBy { it.repository }
                .flatMap { (k, _) ->
                    if (k == null) throw IllegalStateException("Cannot map project without repository")
                    val project =
                        projectMapper.toDomain(
                            k.project,
                        )
                    return@flatMap project.repo?.commits ?: emptySet()
                }
        }

        override fun findAll(pageable: Pageable): Page<Commit> {
            TODO("Not yet implemented")
        }

        @MappingSession
        @Transactional(readOnly = true)
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

                commitMapper.toDomain(it).also { c -> repository.commits.add(c) }
            }

        @MappingSession
        @Transactional
        override fun update(value: Commit): Commit {
            val repositoryId =
                value.repository?.id?.toLong() ?: throw IllegalArgumentException("projectId of Repository must not be null")
            val repository =
                repositoryDao.findById(repositoryId)
                    ?: throw NotFoundException("Repository ${value.repository?.id} not found")

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
                        ctx.entity.branch["${repository.localPath},${it.name}"]
                    if (existing == null) {
//                    create new branch if not exists
                        val newBranch =
                            branchMapper
                                .toEntity(
                                    it,
                                ).also { branch ->
                                    repository.addBranch(branch)
                                }
                        return@map branchDao.create(newBranch)
                    }
                    return@map existing
                },
            )
            entity.committer =
                value.committer?.let { user ->
                    val existing = ctx.entity.user["${repository.localPath},${user.name}"]
                    if (existing == null) {
                        val newUser =
                            userMapper
                                .toEntity(
                                    user,
                                ).also {
                                    repository.addUser(it)
                                }
                        return@let userDao.create(newUser)
                    }
                    return@let existing
                }
            entity.author =
                value.author?.let { user ->
                    val existing = ctx.entity.user["${repository.localPath},${user.name}"]
                    if (existing == null) {
                        val newUser =
                            userMapper
                                .toEntity(
                                    user,
                                ).also {
                                    repository.addUser(it)
                                }
                        return@let userDao.create(newUser)
                    }
                    return@let existing
                }

            entity.branches.removeAll { !value.branches.map { b -> b.name }.contains(it.name) }
            repository.branches.removeAll { !value.branches.map { b -> b.name }.contains(it.name) }

            return this.commitDao
                .update(entity)
                .also { commitDao.flush() }
                .let {
                    val project =
                        projectMapper.toDomain(
                            repository.project,
                        )

                    val repository =
                        repositoryMapper.toDomain(repository, project)
                    ctx.domain.commit.putAll(repository.commits.associateBy { c -> c.sha })
                    ctx.domain.branch.putAll(
                        repository.branches.associateBy { b -> "${repository.localPath},${b.name}" },
                    )
                    ctx.domain.user.putAll(repository.user.associateBy { u -> "${repository.localPath},${u.name}" })
//                    commitMapper.toDomain(it).also { c -> repository.addCommit(c) }
                    commitMapper.toDomainFull(it, repository)
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
                value.repository?.id?.toLong() ?: throw IllegalArgumentException("projectId of Repository must not be null")
            val repository =
                repositoryDao.findById(repositoryId)
                    ?: throw NotFoundException("Repository ${value.repository?.id} not found")

            val mapped =
                commitMapper.toEntity(value).also {
                    repository.addCommit(it)
                }
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

        override fun findFilesByCommitId(commitId: String, pageable: Pageable): Page<File> {
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

        override fun findCommitStatsByCommitId(commitId: String): Stats {
            TODO("Not yet implemented")
        }

        override fun findFileStatsByCommitId(commitId: String): Map<String, Stats> {
            TODO("Not yet implemented")
        }

        override fun findFileActionsByCommitId(commitId: String): Map<String, String?> {
            TODO("Not yet implemented")
        }

        override fun findFileOwnershipByCommitAndFile(commitId: String, fileId: String): List<FileOwnership> {
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
                repositoryDao.findByName(repo.localPath) ?: throw NotFoundException("Repository ${repo.localPath} not found")

            val project =
                projectMapper.toDomain(
                    repoEntity.project,
                )

            val repoModel = repositoryMapper.toDomain(repoEntity, project)

            return this.commitDao
                .findExistingSha(repo, shas)
                .map {
                    this.commitMapper.toDomain(it).also { c -> repoModel.commits.add(c) }
                }
        }

        @MappingSession
        @Transactional(readOnly = true)
        override fun findAll(
            repo: Repository,
            pageable: Pageable,
        ): Iterable<Commit> {
            val repoEntity =
                repositoryDao.findByName(repo.localPath) ?: throw NotFoundException("Repository ${repo.localPath} not found")

            val project =
                projectMapper.toDomain(
                    repoEntity.project,
                )

            val repoModel = repositoryMapper.toDomain(repoEntity, project)
            return try {
                this.commitDao
                    .findAll(
                        repo,
//                        pageable,
                    ).map { this.commitMapper.toDomain(it).also { c -> repoModel.commits.add(c) } }
                    .collect(Collectors.toSet())
            } catch (e: PersistenceException) {
                throw BinocularInfrastructureException(e)
            }
        }

        @MappingSession
        @Transactional(readOnly = true)
        override fun findAll(repository: Repository): Iterable<Commit> =
            this.commitDao
                .findAll(repository)
                .collect(Collectors.toSet())
                .let {
                    return@let this.commitMapper.toDomainFull(it, repository)
                }

        @MappingSession
        @Transactional(readOnly = true)
        override fun findHeadForBranch(
            repo: Repository,
            branch: String,
        ): Commit? {
            val repoEntity =
                repositoryDao.findByName(repo.localPath) ?: throw NotFoundException("Repository ${repo.localPath} not found")

            val project =
                projectMapper.toDomain(
                    repoEntity.project,
                )

            val repoModel = repositoryMapper.toDomain(repoEntity, project)
            return this.commitDao
                .findHeadForBranch(
                    repo,
                    branch,
                )?.let { this.commitMapper.toDomain(it).also { c -> repoModel.commits.add(c) } }
        }

        @MappingSession
        @Transactional(readOnly = true)
        override fun findAllLeafCommits(repo: Repository): Iterable<Commit> {
            val repoEntity =
                repositoryDao.findByName(repo.localPath) ?: throw NotFoundException("Repository ${repo.localPath} not found")

            val project =
                projectMapper.toDomain(
                    repoEntity.project,
                )

            val repoModel = repositoryMapper.toDomain(repoEntity, project)
            return this.commitDao
                .findAllLeafCommits(
                    repo,
                ).map { this.commitMapper.toDomain(it).also { c -> repoModel.commits.add(c) } }
        }
    }
