package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.edge.domain.CommitFileUserConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IFileDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IUserDao
import com.inso_world.binocular.web.persistence.entity.sql.CommitFileUserConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class CommitFileUserConnectionMapper @Autowired constructor(
    private val fileDao: IFileDao,
    private val userDao: IUserDao
) : EntityMapper<CommitFileUserConnection, CommitFileUserConnectionEntity> {

    /**
     * Converts a domain CommitFileUserConnection to a SQL CommitFileUserConnectionEntity
     */
    override fun toEntity(domain: CommitFileUserConnection): CommitFileUserConnectionEntity {
        return CommitFileUserConnectionEntity(
            id = domain.id,
            fileId = domain.from.id ?: throw IllegalStateException("File ID cannot be null"),
            userId = domain.to.id ?: throw IllegalStateException("User ID cannot be null")
        )
    }

    /**
     * Converts a SQL CommitFileUserConnectionEntity to a domain CommitFileUserConnection
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: CommitFileUserConnectionEntity): CommitFileUserConnection {
        val file = fileDao.findById(entity.fileId)
            ?: throw IllegalStateException("File with ID ${entity.fileId} not found")
        val user = userDao.findById(entity.userId)
            ?: throw IllegalStateException("User with ID ${entity.userId} not found")

        return CommitFileUserConnection(
            id = entity.id,
            from = file,
            to = user
        )
    }

    /**
     * Converts a list of SQL CommitFileUserConnectionEntity objects to a list of domain CommitFileUserConnection objects
     */
    override fun toDomainList(entities: Iterable<CommitFileUserConnectionEntity>): List<CommitFileUserConnection> {
        return entities.map { toDomain(it) }
    }
}
