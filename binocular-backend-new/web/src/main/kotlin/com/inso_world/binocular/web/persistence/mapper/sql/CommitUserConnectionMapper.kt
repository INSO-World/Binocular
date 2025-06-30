package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.edge.domain.CommitUserConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IUserDao
import com.inso_world.binocular.web.persistence.entity.sql.CommitUserConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class CommitUserConnectionMapper @Autowired constructor(
    private val commitDao: ICommitDao,
    private val userDao: IUserDao
) : EntityMapper<CommitUserConnection, CommitUserConnectionEntity> {

    /**
     * Converts a domain CommitUserConnection to a SQL CommitUserConnectionEntity
     */
    override fun toEntity(domain: CommitUserConnection): CommitUserConnectionEntity {
        return CommitUserConnectionEntity(
            id = domain.id,
            commitId = domain.from.id ?: throw IllegalStateException("Commit ID cannot be null"),
            userId = domain.to.id ?: throw IllegalStateException("User ID cannot be null")
        )
    }

    /**
     * Converts a SQL CommitUserConnectionEntity to a domain CommitUserConnection
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: CommitUserConnectionEntity): CommitUserConnection {
        val commit = commitDao.findById(entity.commitId)
            ?: throw IllegalStateException("Commit with ID ${entity.commitId} not found")
        val user = userDao.findById(entity.userId)
            ?: throw IllegalStateException("User with ID ${entity.userId} not found")

        return CommitUserConnection(
            id = entity.id,
            from = commit,
            to = user
        )
    }

    /**
     * Converts a list of SQL CommitUserConnectionEntity objects to a list of domain CommitUserConnection objects
     */
    override fun toDomainList(entities: Iterable<CommitUserConnectionEntity>): List<CommitUserConnection> {
        return entities.map { toDomain(it) }
    }
}
