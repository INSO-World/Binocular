package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.edge.domain.CommitCommitConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitCommitConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.web.persistence.entity.sql.CommitEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * SQL implementation of CommitCommitConnectionDao that uses direct JPA relationships
 * instead of intermediate connection entities.
 */
@Repository
@Profile("sql")
@Transactional
class CommitCommitConnectionDao(
    @Autowired private val commitDao: ICommitDao
) : ICommitCommitConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findChildCommits(parentCommitId: String): List<Commit> {
        // Use the direct relationship between Commit and its child Commits
        val query = entityManager.createQuery(
            "SELECT c FROM CommitEntity c WHERE c.id = :parentCommitId",
            CommitEntity::class.java
        )
        query.setParameter("parentCommitId", parentCommitId)
        val commitEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return commitEntity.childCommits.map { commitDao.findById(it.id!!)!! }
    }

    override fun findParentCommits(childCommitId: String): List<Commit> {
        // Use the direct relationship between Commit and its parent Commits
        val query = entityManager.createQuery(
            "SELECT c FROM CommitEntity c WHERE c.id = :childCommitId",
            CommitEntity::class.java
        )
        query.setParameter("childCommitId", childCommitId)
        val commitEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return commitEntity.parentCommits.map { commitDao.findById(it.id!!)!! }
    }

    override fun save(connection: CommitCommitConnection): CommitCommitConnection {
        val fromCommitId = connection.from.id ?: throw IllegalStateException("From Commit ID cannot be null")
        val toCommitId = connection.to.id ?: throw IllegalStateException("To Commit ID cannot be null")

        // Find the entities
        val fromCommitEntity = entityManager.find(CommitEntity::class.java, fromCommitId)
            ?: throw IllegalStateException("From Commit with ID $fromCommitId not found")
        val toCommitEntity = entityManager.find(CommitEntity::class.java, toCommitId)
            ?: throw IllegalStateException("To Commit with ID $toCommitId not found")

        // Add the relationship if it doesn't exist
        if (!fromCommitEntity.childCommits.contains(toCommitEntity)) {
            fromCommitEntity.childCommits.add(toCommitEntity)
            entityManager.merge(fromCommitEntity)
        }

        // Generate a connection ID if not provided
        val connectionId = connection.id ?: UUID.randomUUID().toString()

        // Return the connection with the from and to commits
        return CommitCommitConnection(
            id = connectionId,
            from = commitDao.findById(fromCommitId)!!,
            to = commitDao.findById(toCommitId)!!
        )
    }

    override fun deleteAll() {
        // Clear all relationships between commits
        val commits = entityManager.createQuery("FROM CommitEntity", CommitEntity::class.java).resultList
        for (commit in commits) {
            commit.childCommits.clear()
            entityManager.merge(commit)
        }
    }
}
