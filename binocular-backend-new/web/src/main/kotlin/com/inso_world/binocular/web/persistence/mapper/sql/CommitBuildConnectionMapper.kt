package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.edge.domain.CommitBuildConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IBuildDao
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.web.persistence.entity.sql.CommitBuildConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class CommitBuildConnectionMapper @Autowired constructor(
    private val commitDao: ICommitDao,
    private val buildDao: IBuildDao
) : EntityMapper<CommitBuildConnection, CommitBuildConnectionEntity> {

    /**
     * Converts a domain CommitBuildConnection to a SQL CommitBuildConnectionEntity
     */
    override fun toEntity(domain: CommitBuildConnection): CommitBuildConnectionEntity {
        return CommitBuildConnectionEntity(
            id = domain.id,
            commitId = domain.from.id ?: throw IllegalStateException("Commit ID cannot be null"),
            buildId = domain.to.id ?: throw IllegalStateException("Build ID cannot be null")
        )
    }

    /**
     * Converts a SQL CommitBuildConnectionEntity to a domain CommitBuildConnection
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: CommitBuildConnectionEntity): CommitBuildConnection {
        val commit = commitDao.findById(entity.commitId)
            ?: throw IllegalStateException("Commit with ID ${entity.commitId} not found")
        val build = buildDao.findById(entity.buildId)
            ?: throw IllegalStateException("Build with ID ${entity.buildId} not found")

        return CommitBuildConnection(
            id = entity.id,
            from = commit,
            to = build
        )
    }

    /**
     * Converts a list of SQL CommitBuildConnectionEntity objects to a list of domain CommitBuildConnection objects
     */
    override fun toDomainList(entities: Iterable<CommitBuildConnectionEntity>): List<CommitBuildConnection> {
        return entities.map { toDomain(it) }
    }
}
