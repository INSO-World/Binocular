package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IUserDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import com.inso_world.binocular.infrastructure.sql.persistence.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.sql.persistence.mapper.UserMapper
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Issue
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

/**
 * Implementation of the UserService interface.
 * This service is database-agnostic and works with both ArangoDB and SQL implementations.
 */
@Service
@Validated
internal class UserInfrastructurePortImpl(
    @Autowired private val userDao: IUserDao,
    @Autowired private var userMapper: UserMapper,
) : AbstractInfrastructurePort<User, UserEntity, Long>(Long::class),
    UserInfrastructurePort {
    var logger: Logger = LoggerFactory.getLogger(UserInfrastructurePortImpl::class.java)

    @Autowired
    @Lazy
    private lateinit var repositoryMapper: RepositoryMapper

    @PostConstruct
    fun init() {
        super.dao = userDao
    }

    override fun findById(id: String): User? {
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
        TODO("Not yet implemented")
    }

    override fun delete(value: User) {
        TODO("Not yet implemented")
    }

    override fun updateAndFlush(value: User): User {
        TODO("Not yet implemented")
    }

    override fun create(value: User): User {
        TODO("Not yet implemented")
    }

    override fun saveAll(values: Collection<User>): Iterable<User> {
        TODO("Not yet implemented")
    }

    override fun findAll(): Iterable<User> {
        val context: MutableMap<Long, Repository> = mutableMapOf()
        val commitContext = mutableMapOf<String, Commit>()
        val branchContext = mutableMapOf<String, Branch>()
        val userContext = mutableMapOf<String, User>()

        return super<AbstractInfrastructurePort>.findAllEntities().map { u ->
            val repo =
                context.getOrPut(u.repository?.id!!) {
                    this.repositoryMapper.toDomain(u.repository!!, commitContext, branchContext, userContext)
                }
            userMapper.toDomain(u, repo, userContext, commitContext, branchContext)
        }
    }

    override fun findAll(repository: Repository): Iterable<User> {
        val commitContext = mutableMapOf<String, Commit>()
        val branchContext = mutableMapOf<String, Branch>()
        val userContext = mutableMapOf<String, User>()

        return this.userDao.findAll(repository).map {
            userMapper.toDomain(it, repository, userContext, commitContext, branchContext)
        }
    }

    override fun findAll(pageable: Pageable): Page<User> {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: String) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        super.deleteAllEntities()
    }
}
