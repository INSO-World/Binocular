package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.edge.domain.IssueUserConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IUserDao
import com.inso_world.binocular.web.persistence.entity.sql.IssueUserConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class IssueUserConnectionMapper @Autowired constructor(
    private val issueDao: IIssueDao,
    private val userDao: IUserDao
) : EntityMapper<IssueUserConnection, IssueUserConnectionEntity> {

    /**
     * Converts a domain IssueUserConnection to a SQL IssueUserConnectionEntity
     */
    override fun toEntity(domain: IssueUserConnection): IssueUserConnectionEntity {
        return IssueUserConnectionEntity(
            id = domain.id,
            issueId = domain.from.id ?: throw IllegalStateException("Issue ID cannot be null"),
            userId = domain.to.id ?: throw IllegalStateException("User ID cannot be null")
        )
    }

    /**
     * Converts a SQL IssueUserConnectionEntity to a domain IssueUserConnection
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: IssueUserConnectionEntity): IssueUserConnection {
        val issue = issueDao.findById(entity.issueId)
            ?: throw IllegalStateException("Issue with ID ${entity.issueId} not found")
        val user = userDao.findById(entity.userId)
            ?: throw IllegalStateException("User with ID ${entity.userId} not found")

        return IssueUserConnection(
            id = entity.id,
            from = issue,
            to = user
        )
    }

    /**
     * Converts a list of SQL IssueUserConnectionEntity objects to a list of domain IssueUserConnection objects
     */
    override fun toDomainList(entities: Iterable<IssueUserConnectionEntity>): List<IssueUserConnection> {
        return entities.map { toDomain(it) }
    }
}
