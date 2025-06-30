package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.edge.domain.IssueMilestoneConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMilestoneDao
import com.inso_world.binocular.web.persistence.entity.sql.IssueMilestoneConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class IssueMilestoneConnectionMapper @Autowired constructor(
    private val issueDao: IIssueDao,
    private val milestoneDao: IMilestoneDao
) : EntityMapper<IssueMilestoneConnection, IssueMilestoneConnectionEntity> {

    /**
     * Converts a domain IssueMilestoneConnection to a SQL IssueMilestoneConnectionEntity
     */
    override fun toEntity(domain: IssueMilestoneConnection): IssueMilestoneConnectionEntity {
        return IssueMilestoneConnectionEntity(
            id = domain.id,
            issueId = domain.from.id ?: throw IllegalStateException("Issue ID cannot be null"),
            milestoneId = domain.to.id ?: throw IllegalStateException("Milestone ID cannot be null")
        )
    }

    /**
     * Converts a SQL IssueMilestoneConnectionEntity to a domain IssueMilestoneConnection
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: IssueMilestoneConnectionEntity): IssueMilestoneConnection {
        val issue = issueDao.findById(entity.issueId)
            ?: throw IllegalStateException("Issue with ID ${entity.issueId} not found")
        val milestone = milestoneDao.findById(entity.milestoneId)
            ?: throw IllegalStateException("Milestone with ID ${entity.milestoneId} not found")

        return IssueMilestoneConnection(
            id = entity.id,
            from = issue,
            to = milestone
        )
    }

    /**
     * Converts a list of SQL IssueMilestoneConnectionEntity objects to a list of domain IssueMilestoneConnection objects
     */
    override fun toDomainList(entities: Iterable<IssueMilestoneConnectionEntity>): List<IssueMilestoneConnection> {
        return entities.map { toDomain(it) }
    }
}
