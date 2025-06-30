package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Milestone
import com.inso_world.binocular.web.entity.edge.domain.MergeRequestMilestoneConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestMilestoneConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMilestoneDao
import com.inso_world.binocular.web.persistence.entity.sql.MergeRequestMilestoneConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.sql.MergeRequestMilestoneConnectionMapper
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
@Profile("sql")
@Transactional
class MergeRequestMilestoneConnectionDao(
    @Autowired private val mergeRequestMilestoneConnectionMapper: MergeRequestMilestoneConnectionMapper,
    @Autowired private val mergeRequestDao: IMergeRequestDao,
    @Autowired private val milestoneDao: IMilestoneDao
) : IMergeRequestMilestoneConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findMilestonesByMergeRequest(mergeRequestId: String): List<Milestone> {
        val query = entityManager.createQuery(
            "SELECT m FROM MilestoneEntity m JOIN MergeRequestMilestoneConnectionEntity c ON m.id = c.milestoneId WHERE c.mergeRequestId = :mergeRequestId",
            com.inso_world.binocular.web.persistence.entity.sql.MilestoneEntity::class.java
        )
        query.setParameter("mergeRequestId", mergeRequestId)
        val milestoneEntities = query.resultList
        
        // Convert SQL entities to domain models
        return milestoneEntities.map { milestoneDao.findById(it.id!!)!! }
    }

    override fun findMergeRequestsByMilestone(milestoneId: String): List<MergeRequest> {
        val query = entityManager.createQuery(
            "SELECT mr FROM MergeRequestEntity mr JOIN MergeRequestMilestoneConnectionEntity c ON mr.id = c.mergeRequestId WHERE c.milestoneId = :milestoneId",
            com.inso_world.binocular.web.persistence.entity.sql.MergeRequestEntity::class.java
        )
        query.setParameter("milestoneId", milestoneId)
        val mergeRequestEntities = query.resultList
        
        // Convert SQL entities to domain models
        return mergeRequestEntities.map { mergeRequestDao.findById(it.id!!)!! }
    }

    override fun save(connection: MergeRequestMilestoneConnection): MergeRequestMilestoneConnection {
        val entity = mergeRequestMilestoneConnectionMapper.toEntity(connection)
        
        // Generate ID if not provided
        if (entity.id == null) {
            entity.id = UUID.randomUUID().toString()
        }
        
        // Check if entity already exists
        val existingEntity = entityManager.createQuery(
            "FROM MergeRequestMilestoneConnectionEntity WHERE mergeRequestId = :mergeRequestId AND milestoneId = :milestoneId",
            MergeRequestMilestoneConnectionEntity::class.java
        )
            .setParameter("mergeRequestId", entity.mergeRequestId)
            .setParameter("milestoneId", entity.milestoneId)
            .resultList
            .firstOrNull()
        
        if (existingEntity != null) {
            // Update existing entity
            existingEntity.id = entity.id
            val mergedEntity = entityManager.merge(existingEntity)
            return mergeRequestMilestoneConnectionMapper.toDomain(mergedEntity)
        } else {
            // Create new entity
            entityManager.persist(entity)
            return mergeRequestMilestoneConnectionMapper.toDomain(entity)
        }
    }

    override fun deleteAll() {
        entityManager.createQuery("DELETE FROM MergeRequestMilestoneConnectionEntity").executeUpdate()
    }
}
