package com.inso_world.binocular.web.persistence.dao.sql.connection

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.edge.domain.IssueCommitConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueCommitConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueDao
import com.inso_world.binocular.web.persistence.entity.sql.CommitEntity
import com.inso_world.binocular.web.persistence.entity.sql.IssueEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * SQL implementation of IssueCommitConnectionDao that uses direct JPA relationships
 * instead of intermediate connection entities.
 */
@Repository
@Profile("sql")
@Transactional
class IssueCommitConnectionDao(
    @Autowired private val issueDao: IIssueDao,
    @Autowired private val commitDao: ICommitDao
) : IIssueCommitConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findCommitsByIssue(issueId: String): List<Commit> {
        // Use the direct relationship between Issue and Commit
        val query = entityManager.createQuery(
            "SELECT i FROM IssueEntity i WHERE i.id = :issueId",
            IssueEntity::class.java
        )
        query.setParameter("issueId", issueId)
        val issueEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return issueEntity.commits.map { commitDao.findById(it.id!!)!! }
    }

    override fun findIssuesByCommit(commitId: String): List<Issue> {
        // Use the direct relationship between Commit and Issue
        val query = entityManager.createQuery(
            "SELECT c FROM CommitEntity c WHERE c.id = :commitId",
            CommitEntity::class.java
        )
        query.setParameter("commitId", commitId)
        val commitEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return commitEntity.issues.map { issueDao.findById(it.id!!)!! }
    }

    override fun save(connection: IssueCommitConnection): IssueCommitConnection {
        val issueId = connection.from.id ?: throw IllegalStateException("Issue ID cannot be null")
        val commitId = connection.to.id ?: throw IllegalStateException("Commit ID cannot be null")

        // Find the entities
        val issueEntity = entityManager.find(IssueEntity::class.java, issueId)
            ?: throw IllegalStateException("Issue with ID $issueId not found")
        val commitEntity = entityManager.find(CommitEntity::class.java, commitId)
            ?: throw IllegalStateException("Commit with ID $commitId not found")

        // Add the relationship if it doesn't exist
        if (!issueEntity.commits.contains(commitEntity)) {
            issueEntity.commits.add(commitEntity)
            entityManager.merge(issueEntity)
        }

        // Generate a connection ID if not provided
        val connectionId = connection.id ?: UUID.randomUUID().toString()

        // Return the connection with the issue and commit
        return IssueCommitConnection(
            id = connectionId,
            from = issueDao.findById(issueId)!!,
            to = commitDao.findById(commitId)!!
        )
    }

    override fun deleteAll() {
        // Clear all relationships between issues and commits
        val issues = entityManager.createQuery("SELECT i FROM IssueEntity i", IssueEntity::class.java)
            .resultList
        issues.forEach { issue ->
            issue.commits.clear()
            entityManager.merge(issue)
        }
    }
}
