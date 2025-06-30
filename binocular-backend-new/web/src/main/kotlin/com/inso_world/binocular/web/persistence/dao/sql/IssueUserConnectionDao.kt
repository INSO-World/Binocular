package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.entity.edge.domain.IssueUserConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueUserConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IUserDao
import com.inso_world.binocular.web.persistence.entity.sql.IssueEntity
import com.inso_world.binocular.web.persistence.entity.sql.UserEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * SQL implementation of IssueUserConnectionDao that uses direct JPA relationships
 * instead of intermediate connection entities.
 */
@Repository
@Profile("sql")
@Transactional
class IssueUserConnectionDao(
    @Autowired private val issueDao: IIssueDao,
    @Autowired private val userDao: IUserDao
) : IIssueUserConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findUsersByIssue(issueId: String): List<User> {
        // Use the direct relationship between Issue and User
        val query = entityManager.createQuery(
            "SELECT i FROM IssueEntity i WHERE i.id = :issueId",
            IssueEntity::class.java
        )
        query.setParameter("issueId", issueId)
        val issueEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return issueEntity.users.map { userDao.findById(it.id!!)!! }
    }

    override fun findIssuesByUser(userId: String): List<Issue> {
        // Use the direct relationship between User and Issue
        val query = entityManager.createQuery(
            "SELECT u FROM UserEntity u WHERE u.id = :userId",
            UserEntity::class.java
        )
        query.setParameter("userId", userId)
        val userEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return userEntity.issues.map { issueDao.findById(it.id!!)!! }
    }

    override fun save(connection: IssueUserConnection): IssueUserConnection {
        val issueId = connection.from.id ?: throw IllegalStateException("Issue ID cannot be null")
        val userId = connection.to.id ?: throw IllegalStateException("User ID cannot be null")

        // Find the entities
        val issueEntity = entityManager.find(IssueEntity::class.java, issueId)
            ?: throw IllegalStateException("Issue with ID $issueId not found")
        val userEntity = entityManager.find(UserEntity::class.java, userId)
            ?: throw IllegalStateException("User with ID $userId not found")

        // Add the relationship if it doesn't exist
        if (!issueEntity.users.contains(userEntity)) {
            issueEntity.users.add(userEntity)
            entityManager.merge(issueEntity)
        }

        // Generate a connection ID if not provided
        val connectionId = connection.id ?: UUID.randomUUID().toString()

        // Return the connection with the issue and user
        return IssueUserConnection(
            id = connectionId,
            from = issueDao.findById(issueId)!!,
            to = userDao.findById(userId)!!
        )
    }

    override fun deleteAll() {
        // Clear all relationships between issues and users
        val issues = entityManager.createQuery("FROM IssueEntity", IssueEntity::class.java).resultList
        for (issue in issues) {
            issue.users.clear()
            entityManager.merge(issue)
        }
    }
}
