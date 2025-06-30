package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.edge.domain.MergeRequestMilestoneConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMilestoneDao
import com.inso_world.binocular.web.persistence.entity.sql.MergeRequestMilestoneConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class MergeRequestMilestoneConnectionMapper @Autowired constructor(
    private val mergeRequestDao: IMergeRequestDao,
    private val milestoneDao: IMilestoneDao
) : EntityMapper<MergeRequestMilestoneConnection, MergeRequestMilestoneConnectionEntity> {

    /**
     * Converts a domain MergeRequestMilestoneConnection to a SQL MergeRequestMilestoneConnectionEntity
     */
    override fun toEntity(domain: MergeRequestMilestoneConnection): MergeRequestMilestoneConnectionEntity {
        return MergeRequestMilestoneConnectionEntity(
            id = domain.id,
            mergeRequestId = domain.from.id ?: throw IllegalStateException("MergeRequest ID cannot be null"),
            milestoneId = domain.to.id ?: throw IllegalStateException("Milestone ID cannot be null")
        )
    }

    /**
     * Converts a SQL MergeRequestMilestoneConnectionEntity to a domain MergeRequestMilestoneConnection
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: MergeRequestMilestoneConnectionEntity): MergeRequestMilestoneConnection {
        val mergeRequest = mergeRequestDao.findById(entity.mergeRequestId)
            ?: throw IllegalStateException("MergeRequest with ID ${entity.mergeRequestId} not found")
        val milestone = milestoneDao.findById(entity.milestoneId)
            ?: throw IllegalStateException("Milestone with ID ${entity.milestoneId} not found")

        return MergeRequestMilestoneConnection(
            id = entity.id,
            from = mergeRequest,
            to = milestone
        )
    }

    /**
     * Converts a list of SQL MergeRequestMilestoneConnectionEntity objects to a list of domain MergeRequestMilestoneConnection objects
     */
    override fun toDomainList(entities: Iterable<MergeRequestMilestoneConnectionEntity>): List<MergeRequestMilestoneConnection> {
        return entities.map { toDomain(it) }
    }
}
