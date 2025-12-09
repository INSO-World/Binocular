package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.assembler.RepositoryAssembler
import com.inso_world.binocular.infrastructure.sql.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.sql.persistence.dao.RepositoryDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IDeveloperDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.DeveloperEntity
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import jakarta.annotation.PostConstruct
import jakarta.validation.Valid
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
    @Autowired private val developerDao: IDeveloperDao,
    @Autowired private val repositoryDao: RepositoryDao,
    @Autowired private var repositoryMapper: RepositoryMapper,
    @Autowired private var projectMapper: ProjectMapper,
    @Autowired private var repositoryAssembler: RepositoryAssembler,
) : AbstractInfrastructurePort<User, DeveloperEntity, Long>(Long::class),
    UserInfrastructurePort {
    companion object {
        val logger by logger()
    }

    @PostConstruct
    fun init() {
        super.dao = developerDao
    }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findById(id: String): User? {
        logger.trace("Finding user with id: $id")
        return this.developerDao.findById(id.toLong())?.toLegacyUser()
    }

    override fun findByIid(iid: User.Id): @Valid User? {
        TODO("Not yet implemented")
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

    override fun update(value: User): User {
        throw UnsupportedOperationException("User API is deprecated; use Repository aggregate")
    }


    override fun create(value: User): User {
        throw UnsupportedOperationException("User API is deprecated; use Repository aggregate")
    }

    override fun saveAll(values: Collection<User>): Iterable<User> {
        throw UnsupportedOperationException("User API is deprecated; use Repository aggregate")
    }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findAll(): Iterable<User> {
        val developers = super<AbstractInfrastructurePort>.findAllEntities()
        return developers.mapNotNull { it.toLegacyUser() }
    }

    @MappingSession
    override fun findAll(repository: Repository): Iterable<User> {
        return emptyList()
    }

    override fun findAll(pageable: Pageable): Page<User> {
        TODO("Not yet implemented")
    }

    private fun DeveloperEntity.toLegacyUser(): User? {
        val repositoryDomain = repositoryAssembler.toDomain(this.repository)
        return User(name = this.name, repository = repositoryDomain).apply {
            this.id = this@toLegacyUser.id?.toString()
            this.email = this@toLegacyUser.email
        }
    }
}
