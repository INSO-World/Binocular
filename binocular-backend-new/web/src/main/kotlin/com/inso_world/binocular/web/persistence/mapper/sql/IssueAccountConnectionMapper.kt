package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.edge.domain.IssueAccountConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IAccountDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueDao
import com.inso_world.binocular.web.persistence.entity.sql.IssueAccountConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class IssueAccountConnectionMapper @Autowired constructor(
    private val issueDao: IIssueDao,
    private val accountDao: IAccountDao
) : EntityMapper<IssueAccountConnection, IssueAccountConnectionEntity> {

    /**
     * Converts a domain IssueAccountConnection to a SQL IssueAccountConnectionEntity
     */
    override fun toEntity(domain: IssueAccountConnection): IssueAccountConnectionEntity {
        return IssueAccountConnectionEntity(
            id = domain.id,
            issueId = domain.from.id ?: throw IllegalStateException("Issue ID cannot be null"),
            accountId = domain.to.id ?: throw IllegalStateException("Account ID cannot be null")
        )
    }

    /**
     * Converts a SQL IssueAccountConnectionEntity to a domain IssueAccountConnection
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: IssueAccountConnectionEntity): IssueAccountConnection {
        val issue = issueDao.findById(entity.issueId)
            ?: throw IllegalStateException("Issue with ID ${entity.issueId} not found")
        val account = accountDao.findById(entity.accountId)
            ?: throw IllegalStateException("Account with ID ${entity.accountId} not found")

        return IssueAccountConnection(
            id = entity.id,
            from = issue,
            to = account
        )
    }

    /**
     * Converts a list of SQL IssueAccountConnectionEntity objects to a list of domain IssueAccountConnection objects
     */
    override fun toDomainList(entities: Iterable<IssueAccountConnectionEntity>): List<IssueAccountConnection> {
        return entities.map { toDomain(it) }
    }
}
