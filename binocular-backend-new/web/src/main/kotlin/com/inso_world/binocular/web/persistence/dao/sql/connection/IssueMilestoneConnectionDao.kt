package com.inso_world.binocular.web.persistence.dao.sql.connection

import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.Milestone
import com.inso_world.binocular.web.entity.edge.domain.IssueMilestoneConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueMilestoneConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMilestoneDao
import com.inso_world.binocular.web.persistence.entity.sql.IssueEntity
import com.inso_world.binocular.web.persistence.entity.sql.MilestoneEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * SQL implementation of IssueMilestoneConnectionDao that uses direct JPA relationships
 * instead of intermediate connection entities.
 */
@Repository
@Profile("sql")
@Transactional
class IssueMilestoneConnectionDao(
    @Autowired private val issueDao: IIssueDao,
    @Autowired private val milestoneDao: IMilestoneDao
) : IIssueMilestoneConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findMilestonesByIssue(issueId: String): List<Milestone> {
        // Use the direct relationship between Issue and Milestone
        val query = entityManager.createQuery(
            "SELECT i FROM IssueEntity i WHERE i.id = :issueId",
            IssueEntity::class.java
        )
        query.setParameter("issueId", issueId)
        val issueEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return issueEntity.milestones.map { milestoneDao.findById(it.id!!)!! }
    }

    override fun findIssuesByMilestone(milestoneId: String): List<Issue> {
        // Use the direct relationship between Milestone and Issue
        val query = entityManager.createQuery(
            "SELECT m FROM MilestoneEntity m WHERE m.id = :milestoneId",
            MilestoneEntity::class.java
        )
        query.setParameter("milestoneId", milestoneId)
        val milestoneEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return milestoneEntity.issues.map { issueDao.findById(it.id!!)!! }
    }

    override fun save(connection: IssueMilestoneConnection): IssueMilestoneConnection {
        val issueId = connection.from.id ?: throw IllegalStateException("Issue ID cannot be null")
        val milestoneId = connection.to.id ?: throw IllegalStateException("Milestone ID cannot be null")

        // Find the entities
        val issueEntity = entityManager.find(IssueEntity::class.java, issueId)
            ?: throw IllegalStateException("Issue with ID $issueId not found")
        val milestoneEntity = entityManager.find(MilestoneEntity::class.java, milestoneId)
            ?: throw IllegalStateException("Milestone with ID $milestoneId not found")

        // Add the relationship if it doesn't exist
        if (!issueEntity.milestones.contains(milestoneEntity)) {
            issueEntity.milestones.add(milestoneEntity)
            entityManager.merge(issueEntity)
        }

        // Generate a connection ID if not provided
        val connectionId = connection.id ?: UUID.randomUUID().toString()

        // Return the connection with the issue and milestone
        return IssueMilestoneConnection(
            id = connectionId,
            from = issueDao.findById(issueId)!!,
            to = milestoneDao.findById(milestoneId)!!
        )
    }

    override fun deleteAll() {
        // Clear all relationships between issues and milestones
        val issues = entityManager.createQuery("SELECT i FROM IssueEntity i", IssueEntity::class.java)
            .resultList
        issues.forEach { issue ->
            issue.milestones.clear()
            entityManager.merge(issue)
        }
    }
}
