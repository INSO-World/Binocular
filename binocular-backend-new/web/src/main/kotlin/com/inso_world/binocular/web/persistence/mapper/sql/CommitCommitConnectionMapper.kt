package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.edge.domain.CommitCommitConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.web.persistence.entity.sql.CommitCommitConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class CommitCommitConnectionMapper @Autowired constructor(
    private val commitDao: ICommitDao
) : EntityMapper<CommitCommitConnection, CommitCommitConnectionEntity> {

    /**
     * Converts a domain CommitCommitConnection to a SQL CommitCommitConnectionEntity
     */
    override fun toEntity(domain: CommitCommitConnection): CommitCommitConnectionEntity {
        return CommitCommitConnectionEntity(
            id = domain.id,
            fromCommitId = domain.from.id ?: throw IllegalStateException("From Commit ID cannot be null"),
            toCommitId = domain.to.id ?: throw IllegalStateException("To Commit ID cannot be null")
        )
    }

    /**
     * Converts a SQL CommitCommitConnectionEntity to a domain CommitCommitConnection
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: CommitCommitConnectionEntity): CommitCommitConnection {
        val fromCommit = commitDao.findById(entity.fromCommitId)
            ?: throw IllegalStateException("Commit with ID ${entity.fromCommitId} not found")
        val toCommit = commitDao.findById(entity.toCommitId)
            ?: throw IllegalStateException("Commit with ID ${entity.toCommitId} not found")

        return CommitCommitConnection(
            id = entity.id,
            from = fromCommit,
            to = toCommit
        )
    }

    /**
     * Converts a list of SQL CommitCommitConnectionEntity objects to a list of domain CommitCommitConnection objects
     */
    override fun toDomainList(entities: Iterable<CommitCommitConnectionEntity>): List<CommitCommitConnection> {
        return entities.map { toDomain(it) }
    }
}
