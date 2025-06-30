package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Milestone
import com.inso_world.binocular.web.entity.edge.domain.MergeRequestMilestoneConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestMilestoneConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMilestoneDao
import com.inso_world.binocular.web.persistence.entity.sql.MergeRequestEntity
import com.inso_world.binocular.web.persistence.entity.sql.MilestoneEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * SQL implementation of MergeRequestMilestoneConnectionDao that uses direct JPA relationships
 * instead of intermediate connection entities.
 */
@Repository
@Profile("sql")
@Transactional
class MergeRequestMilestoneConnectionDao(
    @Autowired private val mergeRequestDao: IMergeRequestDao,
    @Autowired private val milestoneDao: IMilestoneDao
) : IMergeRequestMilestoneConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findMilestonesByMergeRequest(mergeRequestId: String): List<Milestone> {
        // Use the direct relationship between MergeRequest and Milestone
        val query = entityManager.createQuery(
            "SELECT mr FROM MergeRequestEntity mr WHERE mr.id = :mergeRequestId",
            MergeRequestEntity::class.java
        )
        query.setParameter("mergeRequestId", mergeRequestId)
        val mergeRequestEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return mergeRequestEntity.milestones.map { milestoneDao.findById(it.id!!)!! }
    }

    override fun findMergeRequestsByMilestone(milestoneId: String): List<MergeRequest> {
        // Use the direct relationship between Milestone and MergeRequest
        val query = entityManager.createQuery(
            "SELECT m FROM MilestoneEntity m WHERE m.id = :milestoneId",
            MilestoneEntity::class.java
        )
        query.setParameter("milestoneId", milestoneId)
        val milestoneEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return milestoneEntity.mergeRequests.map { mergeRequestDao.findById(it.id!!)!! }
    }

    override fun save(connection: MergeRequestMilestoneConnection): MergeRequestMilestoneConnection {
        val mergeRequestId = connection.from.id ?: throw IllegalStateException("MergeRequest ID cannot be null")
        val milestoneId = connection.to.id ?: throw IllegalStateException("Milestone ID cannot be null")

        // Find the entities
        val mergeRequestEntity = entityManager.find(MergeRequestEntity::class.java, mergeRequestId)
            ?: throw IllegalStateException("MergeRequest with ID $mergeRequestId not found")
        val milestoneEntity = entityManager.find(MilestoneEntity::class.java, milestoneId)
            ?: throw IllegalStateException("Milestone with ID $milestoneId not found")

        // Add the relationship if it doesn't exist
        if (!mergeRequestEntity.milestones.contains(milestoneEntity)) {
            mergeRequestEntity.milestones.add(milestoneEntity)
            entityManager.merge(mergeRequestEntity)
        }

        // Generate a connection ID if not provided
        val connectionId = connection.id ?: UUID.randomUUID().toString()

        // Return the connection with the mergeRequest and milestone
        return MergeRequestMilestoneConnection(
            id = connectionId,
            from = mergeRequestDao.findById(mergeRequestId)!!,
            to = milestoneDao.findById(milestoneId)!!
        )
    }

    override fun deleteAll() {
        // Clear all relationships between mergeRequests and milestones
        val mergeRequests = entityManager.createQuery("FROM MergeRequestEntity", MergeRequestEntity::class.java).resultList
        for (mergeRequest in mergeRequests) {
            mergeRequest.milestones.clear()
            entityManager.merge(mergeRequest)
        }
    }
}
