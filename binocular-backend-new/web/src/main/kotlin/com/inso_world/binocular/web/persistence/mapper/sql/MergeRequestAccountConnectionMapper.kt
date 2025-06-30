package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.edge.domain.MergeRequestAccountConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IAccountDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestDao
import com.inso_world.binocular.web.persistence.entity.sql.MergeRequestAccountConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class MergeRequestAccountConnectionMapper @Autowired constructor(
    private val mergeRequestDao: IMergeRequestDao,
    private val accountDao: IAccountDao
) : EntityMapper<MergeRequestAccountConnection, MergeRequestAccountConnectionEntity> {

    /**
     * Converts a domain MergeRequestAccountConnection to a SQL MergeRequestAccountConnectionEntity
     */
    override fun toEntity(domain: MergeRequestAccountConnection): MergeRequestAccountConnectionEntity {
        return MergeRequestAccountConnectionEntity(
            id = domain.id,
            mergeRequestId = domain.from.id ?: throw IllegalStateException("MergeRequest ID cannot be null"),
            accountId = domain.to.id ?: throw IllegalStateException("Account ID cannot be null")
        )
    }

    /**
     * Converts a SQL MergeRequestAccountConnectionEntity to a domain MergeRequestAccountConnection
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: MergeRequestAccountConnectionEntity): MergeRequestAccountConnection {
        val mergeRequest = mergeRequestDao.findById(entity.mergeRequestId)
            ?: throw IllegalStateException("MergeRequest with ID ${entity.mergeRequestId} not found")
        val account = accountDao.findById(entity.accountId)
            ?: throw IllegalStateException("Account with ID ${entity.accountId} not found")

        return MergeRequestAccountConnection(
            id = entity.id,
            from = mergeRequest,
            to = account
        )
    }

    /**
     * Converts a list of SQL MergeRequestAccountConnectionEntity objects to a list of domain MergeRequestAccountConnection objects
     */
    override fun toDomainList(entities: Iterable<MergeRequestAccountConnectionEntity>): List<MergeRequestAccountConnection> {
        return entities.map { toDomain(it) }
    }
}
