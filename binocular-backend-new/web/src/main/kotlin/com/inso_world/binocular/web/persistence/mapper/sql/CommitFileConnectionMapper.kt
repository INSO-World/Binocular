package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.edge.domain.CommitFileConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IFileDao
import com.inso_world.binocular.web.persistence.entity.sql.CommitFileConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class CommitFileConnectionMapper @Autowired constructor(
    private val commitDao: ICommitDao,
    private val fileDao: IFileDao
) : EntityMapper<CommitFileConnection, CommitFileConnectionEntity> {

    /**
     * Converts a domain CommitFileConnection to a SQL CommitFileConnectionEntity
     */
    override fun toEntity(domain: CommitFileConnection): CommitFileConnectionEntity {
        return CommitFileConnectionEntity(
            id = domain.id,
            commitId = domain.from.id ?: throw IllegalStateException("Commit ID cannot be null"),
            fileId = domain.to.id ?: throw IllegalStateException("File ID cannot be null"),
            lineCount = domain.lineCount
        )
    }

    /**
     * Converts a SQL CommitFileConnectionEntity to a domain CommitFileConnection
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: CommitFileConnectionEntity): CommitFileConnection {
        val commit = commitDao.findById(entity.commitId)
            ?: throw IllegalStateException("Commit with ID ${entity.commitId} not found")
        val file = fileDao.findById(entity.fileId)
            ?: throw IllegalStateException("File with ID ${entity.fileId} not found")

        return CommitFileConnection(
            id = entity.id,
            from = commit,
            to = file,
            lineCount = entity.lineCount
        )
    }

    /**
     * Converts a list of SQL CommitFileConnectionEntity objects to a list of domain CommitFileConnection objects
     */
    override fun toDomainList(entities: Iterable<CommitFileConnectionEntity>): List<CommitFileConnection> {
        return entities.map { toDomain(it) }
    }
}
