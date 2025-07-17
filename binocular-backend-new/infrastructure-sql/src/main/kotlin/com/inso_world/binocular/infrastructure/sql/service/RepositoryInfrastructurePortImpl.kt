package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.persistence.exception.NotFoundException
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.persistence.dao.CommitDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.RepositoryDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IProjectDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import com.inso_world.binocular.infrastructure.sql.persistence.mapper.BranchMapper
import com.inso_world.binocular.infrastructure.sql.persistence.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.sql.persistence.mapper.RepositoryMapper
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Service
@Validated
internal class RepositoryInfrastructurePortImpl :
    AbstractInfrastructurePort<Repository, RepositoryEntity, Long>(Long::class),
    RepositoryInfrastructurePort {
    private val logger: Logger = LoggerFactory.getLogger(RepositoryInfrastructurePortImpl::class.java)

    @Autowired
    lateinit var repositoryMapper: RepositoryMapper

    @Autowired
    @Lazy
    lateinit var commitMapper: CommitMapper

    @Autowired
    @Lazy
    private lateinit var branchMapper: BranchMapper

    @Autowired
    private lateinit var repositoryDao: RepositoryDao

    @Autowired
    @Lazy
    private lateinit var commitDao: CommitDao

    @Autowired
    private lateinit var projectDao: IProjectDao

    @PostConstruct
    fun init() {
        super.dao = repositoryDao
//        super.mapper = repositoryMapper
    }

    override fun findByName(name: String): Repository? =
        this.repositoryDao.findByName(name)?.let {
            val commitContext = mutableMapOf<String, Commit>()
            val branchContext = mutableMapOf<String, Branch>()
            val userContext = mutableMapOf<String, User>()

            this.repositoryMapper.toDomain(it, commitContext, branchContext, userContext)
        }

    override fun findAll(): Iterable<Repository> {
        val commitContext = mutableMapOf<String, Commit>()
        val branchContext = mutableMapOf<String, Branch>()
        val userContext = mutableMapOf<String, User>()

        return findAllEntities().map {
            this.repositoryMapper.toDomain(it, commitContext, branchContext, userContext)
        }
    }

    override fun findAll(pageable: Pageable): Page<Repository> {
        TODO("Not yet implemented")
//        return this.projectDao.findAll(pageable).map {
//            this.projectMapper.toDomain(it)
//        }
    }

    override fun findById(id: String): Repository? =
        this.repositoryDao.findById(id.toLong())?.let {
            val commitContext = mutableMapOf<String, Commit>()
            val branchContext = mutableMapOf<String, Branch>()
            val userContext = mutableMapOf<String, User>()

            this.repositoryMapper.toDomain(it, commitContext, branchContext, userContext)
        }

    override fun create(value: Repository): Repository {
        val projectId =
            value.projectId?.toLong() ?: throw IllegalArgumentException("projectId of Repository must not be null")
        val project = projectDao.findById(projectId) ?: throw NotFoundException("Project ${value.projectId} not found")

        if (project.repo != null) {
            throw IllegalArgumentException("Selected project $project has already a Repository set")
        }

        val mapped =
            this.repositoryMapper.toEntity(
                value,
                project,
            )
        val newEntity = super.create(mapped)

        val commitContext = mutableMapOf<String, Commit>()
        val branchContext = mutableMapOf<String, Branch>()
        val userContext = mutableMapOf<String, User>()

        return repositoryMapper.toDomain(newEntity, commitContext, branchContext, userContext)
    }

    override fun update(value: Repository): Repository {
        val projectId =
            value.projectId?.toLong() ?: throw IllegalArgumentException("projectId of Repository must not be null")
        val project = projectDao.findById(projectId) ?: throw NotFoundException("Project ${value.projectId} not found")
        val entity =
            project.repo
                ?: throw IllegalStateException("On updating Repository, it is required to be set to project already")

        run {
            // Synchronize commits: remove those not in value.commits
            val valueCommitShas = value.commits.map { it.sha }.toSet()
            entity.commits.removeIf { it.sha !in valueCommitShas }
        }
        run {
            // Synchronize branches: remove those not in value.branches
            val valueBranchKeys = value.branches.map { "${entity.name},${it.name}" }.toSet()
            entity.branches.removeIf { it.uniqueKey() !in valueBranchKeys }
        }
        run {
            // Synchronize user: remove those not in value.user
            val valueUserKeys = value.user.map { it.uniqueKey() }.toSet()
            entity.user.removeIf { it.uniqueKey() !in valueUserKeys }
        }

        // Rebuild context after removals
        val commitContext: MutableMap<String, CommitEntity> = entity.commits.associateBy { it.sha }.toMutableMap()
        val branchContext: MutableMap<String, BranchEntity> =
            entity.branches.associateBy { it.uniqueKey() }.toMutableMap()
        val userContext: MutableMap<String, UserEntity> =
            entity.user.associateBy { it.uniqueKey() }.toMutableMap()

        // Add or update commits
        value.commits.forEach {
            if (!commitContext.containsKey(it.sha)) {
                val commitEntity = commitMapper.toEntity(it, entity, commitContext, branchContext, userContext)
                entity.commits.add(commitEntity)
                commitContext[it.sha] = commitEntity
            }
        }
        // Ensure all commits referenced by branches are present
        value.branches.forEach { branch ->
            branch.commitShas.forEach { sha ->
                if (!commitContext.containsKey(sha)) {
                    val commit = value.commits.find { it.sha == sha }
                    if (commit != null) {
                        val commitEntity =
                            commitMapper.toEntity(commit, entity, commitContext, branchContext, userContext)
                        entity.commits.add(commitEntity)
                        commitContext[sha] = commitEntity
                    } else {
                        throw IllegalStateException("Branch references commit sha $sha not present in value.commits")
                    }
                }
            }
        }
        // Add or update branches
        value.branches.forEach {
            val key = "${entity.name},${it.name}"
            if (!branchContext.containsKey(key)) {
                val newBranch = branchMapper.toEntity(it, entity, commitContext, branchContext)
                entity.branches.add(newBranch)
                branchContext[key] = newBranch
            }
        }

        val updated = this.repositoryDao.update(entity)
        this.repositoryDao.flush()
        return run {
            entityManager.refresh(updated)
            val commitContext = mutableMapOf<String, Commit>()
            val branchContext = mutableMapOf<String, Branch>()
            val userContext = mutableMapOf<String, User>()

            repositoryMapper.toDomain(updated, commitContext, branchContext, userContext)
        }
    }

    override fun updateAndFlush(value: Repository): Repository {
        val updated = update(value)
        repositoryDao.flush()
        return updated
    }

    override fun saveAll(values: Collection<Repository>): Iterable<Repository> {
        TODO("Not yet implemented")
    }

    override fun delete(value: Repository) {
        val mapped =
            this.repositoryDao.findByName(name = value.name)
                ?: throw NotFoundException("Repository ${value.name} not found")

//        val mapped =
//            this.repositoryMapper.toEntity(
//                domain,
//                project,
//            )
        this.repositoryDao.delete(mapped)
    }

    override fun deleteById(id: String) {
        this.repositoryDao.deleteById(id.toLong())
    }

    override fun deleteAll() {
        this.repositoryDao.deleteAll()
    }
}
