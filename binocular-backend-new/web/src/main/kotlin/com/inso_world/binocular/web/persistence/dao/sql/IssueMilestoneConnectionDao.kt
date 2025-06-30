package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.Milestone
import com.inso_world.binocular.web.entity.edge.domain.IssueMilestoneConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueMilestoneConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMilestoneDao
import com.inso_world.binocular.web.persistence.entity.sql.IssueMilestoneConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.sql.IssueMilestoneConnectionMapper
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
class IssueMilestoneConnectionDao(
    @Autowired private val issueMilestoneConnectionMapper: IssueMilestoneConnectionMapper,
    @Autowired private val issueDao: IIssueDao,
    @Autowired private val milestoneDao: IMilestoneDao
) : IIssueMilestoneConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findMilestonesByIssue(issueId: String): List<Milestone> {
        val query = entityManager.createQuery(
            "SELECT m FROM MilestoneEntity m JOIN IssueMilestoneConnectionEntity im ON m.id = im.milestoneId WHERE im.issueId = :issueId",
            com.inso_world.binocular.web.persistence.entity.sql.MilestoneEntity::class.java
        )
        query.setParameter("issueId", issueId)
        val milestoneEntities = query.resultList
        
        // Convert SQL entities to domain models
        return milestoneEntities.map { milestoneDao.findById(it.id!!)!! }
    }

    override fun findIssuesByMilestone(milestoneId: String): List<Issue> {
        val query = entityManager.createQuery(
            "SELECT i FROM IssueEntity i JOIN IssueMilestoneConnectionEntity im ON i.id = im.issueId WHERE im.milestoneId = :milestoneId",
            com.inso_world.binocular.web.persistence.entity.sql.IssueEntity::class.java
        )
        query.setParameter("milestoneId", milestoneId)
        val issueEntities = query.resultList
        
        // Convert SQL entities to domain models
        return issueEntities.map { issueDao.findById(it.id!!)!! }
    }

    override fun save(connection: IssueMilestoneConnection): IssueMilestoneConnection {
        val entity = issueMilestoneConnectionMapper.toEntity(connection)
        
        // Generate ID if not provided
        if (entity.id == null) {
            entity.id = UUID.randomUUID().toString()
        }
        
        // Check if entity already exists
        val existingEntity = entityManager.createQuery(
            "FROM IssueMilestoneConnectionEntity WHERE issueId = :issueId AND milestoneId = :milestoneId",
            IssueMilestoneConnectionEntity::class.java
        )
            .setParameter("issueId", entity.issueId)
            .setParameter("milestoneId", entity.milestoneId)
            .resultList
            .firstOrNull()
        
        if (existingEntity != null) {
            // Update existing entity
            existingEntity.id = entity.id
            val mergedEntity = entityManager.merge(existingEntity)
            return issueMilestoneConnectionMapper.toDomain(mergedEntity)
        } else {
            // Create new entity
            entityManager.persist(entity)
            return issueMilestoneConnectionMapper.toDomain(entity)
        }
    }

    override fun deleteAll() {
        entityManager.createQuery("DELETE FROM IssueMilestoneConnectionEntity").executeUpdate()
    }
}
