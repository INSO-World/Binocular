package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.exception.NotFoundException
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.mapper.BranchMapper
import com.inso_world.binocular.infrastructure.sql.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.sql.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.sql.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.sql.mapper.context.MappingSession
import com.inso_world.binocular.infrastructure.sql.persistence.dao.CommitDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.ProjectDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.RepositoryDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Service
@Validated
internal class RepositoryInfrastructurePortImpl :
    AbstractInfrastructurePort<Repository, RepositoryEntity, Long>(Long::class),
    RepositoryInfrastructurePort {
    companion object {
        private val logger by logger()
    }

    @Autowired
    lateinit var repositoryMapper: RepositoryMapper

    @Autowired
    @Lazy
    lateinit var commitMapper: CommitMapper

    @Autowired
    @Lazy
    lateinit var projectMapper: ProjectMapper

    @Autowired
    @Lazy
    private lateinit var branchMapper: BranchMapper

    @Autowired
    private lateinit var ctx: MappingContext

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
    override fun findByName(name: String): Repository? =
        this.repositoryDao.findByName(name)?.let {
            val project =
                projectMapper.toDomain(
                    it.project,
                )

            this.repositoryMapper.toDomain(it, project)
        }

    @MappingSession
    override fun findAll(): Iterable<Repository> {
        val projectContext = mutableMapOf<String, Project>()

        return findAllEntities().map {
            val project =
                projectContext.getOrPut(it.project.uniqueKey()) {
                    projectMapper.toDomain(
                        it.project,
                    )
                }

            this.repositoryMapper.toDomain(it, project)
        }
    }

    override fun findAll(pageable: Pageable): Page<Repository> {
        TODO("Not yet implemented")
//        return this.projectDao.findAll(pageable).map {
//            this.projectMapper.toDomain(it)
//        }
    }

    @MappingSession
    override fun findById(id: String): Repository? =
        this.repositoryDao.findById(id.toLong())?.let {
            val project =
                projectMapper.toDomain(
                    it.project,
                )

            this.repositoryMapper.toDomain(it, project)
        }

    @MappingSession
    override fun create(value: Repository): Repository {
        val projectId =
            value.project?.id?.toLong() ?: throw IllegalArgumentException("project.id of Repository must not be null")
        val project = projectDao.findById(projectId) ?: throw NotFoundException("Project ${value.project} not found")

        if (project.repo != null) {
            throw IllegalArgumentException("Selected project $project has already a Repository set")
        }

        val mapped =
            this.repositoryMapper.toEntity(
                value,
                project,
            )
//        val newEntity =
        return try {
            val newEntity = super.create(mapped)
            repositoryDao.flush()
            newEntity
        } catch (e: DataIntegrityViolationException) {
            entityManager.flush()
            entityManager.clear()
            logger.error(e.message)
            throw e
        }.let { newEntity ->
            val project =
                projectMapper.toDomain(
                    newEntity.project,
                )

            return@let repositoryMapper.toDomain(newEntity, project)
        }
    }

    @MappingSession
    override fun update(value: Repository): Repository {
        val entity =
            run {
                value.id?.let {
                    repositoryDao.findById(it.toLong())
                } ?: run {
                    val project =
                        value.project?.id?.let { id ->
                            this.projectDao.findById(id.toLong())
                        } ?: throw NotFoundException("Project ${value.project} not found")

                    this.repositoryMapper.toEntity(value, project)
                }
            }

        logger.debug("Repository Entity found")

        run {
            // Synchronize commits: remove those not in value.commits
            val valueCommitShas = value.commits.map { it.sha }.toSet()
            entity.commits.removeIf { it.sha !in valueCommitShas }
        }
        logger.trace("Commits synchronized")
        run {
            // Synchronize branches: remove those not in value.branches
            val valueBranchKeys = value.branches.map { "${entity.localPath},${it.name}" }.toSet()
            entity.branches.removeIf { it.uniqueKey() !in valueBranchKeys }
        }
        logger.trace("Branches synchronized")
        run {
            // Synchronize user: remove those not in value.user
            val valueUserKeys = value.user.map { it.uniqueKey() }.toSet()
            entity.user.removeIf { it.uniqueKey() !in valueUserKeys }
        }
        logger.trace("User synchronized")

        // build context after changes are synced
        // TODO N+1 here
        ctx.entity.commit.putAll(entity.commits.associateBy(CommitEntity::uniqueKey))
        ctx.entity.branch.putAll(entity.branches.associateBy(BranchEntity::uniqueKey))
        ctx.entity.user.putAll(entity.user.associateBy(UserEntity::uniqueKey))
        logger.trace("Entity context built")

//        wireup is done internally
        commitMapper.toEntityFull(
            (value.commits + value.commits.flatMap { it.parents } + value.commits.flatMap { it.children }),
            entity,
        )

        logger.trace("Commits updated")
        // Add or update branches
        value.branches.forEach {
            val key = "${entity.localPath},${it.name}"
            if (!ctx.entity.branch.containsKey(key)) {
                val newBranch = branchMapper.toEntity(it).also { b -> entity.addBranch(b) }
                entity.branches.add(newBranch)
                ctx.entity.branch.computeIfAbsent(key) { newBranch }
            }
        }
        logger.trace("Branches updated")

        entity.commits.filter { it.id == null }.map { commit ->
            commitDao.create(commit)
        }

        val updated = repositoryDao.update(entity).also { repositoryDao.flush() }

        logger.trace("Update executed")

        return run {
            // Instead of refresh + lazy‐walk, grab a fully fetched instance:
            val fullyLoaded =
                repositoryDao
                    .findByIdWithAllRelations(updated.id!!)
                    ?: throw NotFoundException("Repository ${updated.id} disappeared")

            logger.trace("Entity refreshed")
            logger.trace("Domain context built")

            val project = projectMapper.toDomain(fullyLoaded.project)
            logger.trace("Domain project built")

            val domain = repositoryMapper.toDomain(fullyLoaded, project)
            logger.trace("Domain object built")
            return@run domain
        }
    }

    override fun updateAndFlush(value: Repository): Repository {
        val updated = update(value)
        repositoryDao.flush()
        return updated
    }

    override fun saveAll(values: Collection<Repository>): Iterable<Repository> {
        return values.map { this.create(it) }
    }

    override fun delete(value: Repository) {
        val mapped =
            this.repositoryDao.findByName(name = value.localPath)
                ?: throw NotFoundException("Repository ${value.localPath} not found")
        this.repositoryDao.delete(mapped)
    }

    override fun deleteById(id: String) {
        this.repositoryDao.deleteById(id.toLong())
    }

    override fun deleteAll() {
        this.repositoryDao.deleteAll()
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
