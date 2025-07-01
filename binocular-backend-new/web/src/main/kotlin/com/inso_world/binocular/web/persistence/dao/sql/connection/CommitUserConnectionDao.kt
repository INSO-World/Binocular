package com.inso_world.binocular.web.persistence.dao.sql.connection

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.entity.edge.domain.CommitUserConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitUserConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IUserDao
import com.inso_world.binocular.web.persistence.entity.sql.CommitEntity
import com.inso_world.binocular.web.persistence.entity.sql.UserEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * SQL implementation of CommitUserConnectionDao that uses direct JPA relationships
 * instead of intermediate connection entities.
 */
@Repository
@Profile("sql")
@Transactional
class CommitUserConnectionDao(
    @Autowired private val commitDao: ICommitDao,
    @Autowired private val userDao: IUserDao
) : ICommitUserConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findUsersByCommit(commitId: String): List<User> {
        // Use the direct relationship between Commit and User
        val query = entityManager.createQuery(
            "SELECT c FROM CommitEntity c WHERE c.id = :commitId",
            CommitEntity::class.java
        )
        query.setParameter("commitId", commitId)
        val commitEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return commitEntity.users.map { userDao.findById(it.id!!)!! }
    }

    override fun findCommitsByUser(userId: String): List<Commit> {
        // Use the direct relationship between User and Commit
        val query = entityManager.createQuery(
            "SELECT u FROM UserEntity u WHERE u.id = :userId",
            UserEntity::class.java
        )
        query.setParameter("userId", userId)
        val userEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return userEntity.commits.map { commitDao.findById(it.id!!)!! }
    }

    override fun save(connection: CommitUserConnection): CommitUserConnection {
        val commitId = connection.from.id ?: throw IllegalStateException("Commit ID cannot be null")
        val userId = connection.to.id ?: throw IllegalStateException("User ID cannot be null")

        // Find the entities
        val commitEntity = entityManager.find(CommitEntity::class.java, commitId)
            ?: throw IllegalStateException("Commit with ID $commitId not found")
        val userEntity = entityManager.find(UserEntity::class.java, userId)
            ?: throw IllegalStateException("User with ID $userId not found")

        // Add the relationship if it doesn't exist
        if (!commitEntity.users.contains(userEntity)) {
            commitEntity.users.add(userEntity)
            entityManager.merge(commitEntity)
        }

        // Generate a connection ID if not provided
        val connectionId = connection.id ?: UUID.randomUUID().toString()

        // Return the connection with the commit and user
        return CommitUserConnection(
            id = connectionId,
            from = commitDao.findById(commitId)!!,
            to = userDao.findById(userId)!!
        )
    }

    override fun deleteAll() {
        // Clear all relationships between commits and users
        val commits = entityManager.createQuery("SELECT c FROM CommitEntity c", CommitEntity::class.java)
            .resultList
        commits.forEach { commit ->
            commit.users.clear()
            entityManager.merge(commit)
        }
    }
}
