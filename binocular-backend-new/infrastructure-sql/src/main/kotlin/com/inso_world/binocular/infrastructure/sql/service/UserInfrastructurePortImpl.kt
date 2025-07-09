package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IUserDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import com.inso_world.binocular.infrastructure.sql.persistence.mapper.UserMapper
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.User
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

/**
 * Implementation of the UserService interface.
 * This service is database-agnostic and works with both ArangoDB and SQL implementations.
 */
@Service
internal class UserInfrastructurePortImpl(
    @Autowired private val userDao: IUserDao,
    @Autowired private var userMapper: UserMapper,
) : AbstractInfrastructurePort<User, UserEntity, Long>(Long::class),
    UserInfrastructurePort {
    var logger: Logger = LoggerFactory.getLogger(UserInfrastructurePortImpl::class.java)

    @PostConstruct
    fun init() {
        super.mapper = userMapper
        super.dao = userDao
    }

    override fun findAll(pageable: Pageable): Page<User> {
        logger.trace("Getting all users with pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}")
        return super.findAll(pageable)
    }

    override fun findById(id: String): User? {
        logger.trace("Getting user by id: $id")
        return super.findById(id)
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
}
