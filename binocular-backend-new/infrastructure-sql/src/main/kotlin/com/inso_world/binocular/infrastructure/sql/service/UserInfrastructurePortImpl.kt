package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.core.service.exception.NotFoundException
import com.inso_world.binocular.infrastructure.sql.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.sql.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.sql.mapper.UserMapper
import com.inso_world.binocular.infrastructure.sql.mapper.context.MappingSession
import com.inso_world.binocular.infrastructure.sql.persistence.dao.RepositoryDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IUserDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Project
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

/**
 * Implementation of the UserService interface.
 * This service is database-agnostic and works with both ArangoDB and SQL implementations.
 */
@Service
@Validated
internal class UserInfrastructurePortImpl(
    @Autowired private val userDao: IUserDao,
    @Autowired private val repositoryDao: RepositoryDao,
    @Autowired private var userMapper: UserMapper,
    @Autowired private var commitMapper: CommitMapper,
) : AbstractInfrastructurePort<User, UserEntity, Long>(Long::class),
    UserInfrastructurePort {
    var logger: Logger = LoggerFactory.getLogger(UserInfrastructurePortImpl::class.java)

    @Autowired
    @Lazy
    private lateinit var repositoryMapper: RepositoryMapper

    @Autowired
    @Lazy
    private lateinit var projectMapper: ProjectMapper

    @PostConstruct
    fun init() {
        super.dao = userDao
    }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findById(id: String): User? {
        val entity = userDao.findById(id.toLong()) ?: return null
        val repoEntity = entity.repository ?: return null
        val project = projectMapper.toDomain(repoEntity.project)
        val repository = repositoryMapper.toDomain(repoEntity, project)
        return userMapper.toDomainFull(entity, repository)
    }

    override fun findCommitsByUserId(userId: String): List<Commit> {
        TODO("Not yet implemented")
    }

    override fun findIssuesByUserId(userId: String): List<Issue> {
        TODO("Not yet implemented")
    }

    override fun findFilesByUserId(userId: String): List<File> {
        TODO("Not yet implemented")
    }

    @MappingSession
    @Transactional
    override fun update(value: User): User {
        val id = value.id?.toLong() ?: throw NotFoundException("User id is required for update")
        val managed = userDao.findById(id) ?: throw NotFoundException("User ${'$'}id not found")
        managed.name = value.name
        managed.email = value.email
        userDao.update(managed)
        userDao.flush()
        val repoEntity = managed.repository ?: throw IllegalStateException("Repository cannot be null")
        val project = projectMapper.toDomain(repoEntity.project)
        val repository = repositoryMapper.toDomain(repoEntity, project)
        return userMapper.toDomainFull(managed, repository)
    }

    @Transactional
    override fun delete(value: User) {
        value.id?.let { deleteById(it) }
    }

    @Transactional
    override fun updateAndFlush(value: User): User = update(value)

    @MappingSession
    @Transactional
    override fun create(value: User): User {
        val repo = value.repository ?: throw IllegalArgumentException("repository must not be null")
        val repoId = repo.id?.toLong()
        val repoEntity = when {
            repoId != null -> repositoryDao.findById(repoId) ?: throw NotFoundException("Repository ${'$'}repoId not found")
            else -> repositoryDao.findByName(repo.localPath) ?: throw NotFoundException("Repository ${'$'}{repo.localPath} not found")
        }
        val entity = userMapper.toEntity(value)
        entity.repository = repoEntity
        val created = userDao.create(entity)
        userDao.flush()
        val project = projectMapper.toDomain(repoEntity.project)
        val repository = repositoryMapper.toDomain(repoEntity, project)
        return userMapper.toDomainFull(created, repository)
    }

    @Transactional
    override fun saveAll(values: Collection<User>): Iterable<User> = values.map { create(it) }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findAll(): Iterable<User> {
        val context: MutableMap<Long, Repository> = mutableMapOf()
        val projectContext = mutableMapOf<String, Project>()

        return super<AbstractInfrastructurePort>.findAllEntities().map { u ->
            val repoEntity = u.repository
            if (repoEntity == null) {
                throw IllegalStateException("Repository cannot be null")
            }
            val project =
                projectContext.getOrPut(repoEntity.project.uniqueKey()) {
                    projectMapper.toDomain(
                        repoEntity.project,
                    )
                }

            if (repoEntity.id == null) {
                throw IllegalStateException("Id of repository cannot be null")
            }

            val repository =
                context.getOrPut(repoEntity.id) {
                    this.repositoryMapper.toDomain(repoEntity, project)
                }
            userMapper.toDomainFull(u, repository)
        }
    }

    @MappingSession
    override fun findAll(repository: Repository): Iterable<User> {
        TODO()
//        val repoEntity =
//            this.repositoryDao.findByName(repository.localPath) ?: throw NotFoundException("Could not find repository $repository")
//
//        this.userDao
//            .findAll(repoEntity)
//            .map {
//                userMapper.toDomain(it)
//            }
    }

    override fun findAll(pageable: Pageable): Page<User> {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: String) {
        TODO("Not yet implemented")
    }

    @Transactional
    override fun deleteAll() {
        super.deleteAllEntities()
    }
}
