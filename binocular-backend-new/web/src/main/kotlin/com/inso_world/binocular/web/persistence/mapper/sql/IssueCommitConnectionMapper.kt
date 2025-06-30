package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.edge.domain.IssueCommitConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueDao
import com.inso_world.binocular.web.persistence.entity.sql.IssueCommitConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class IssueCommitConnectionMapper @Autowired constructor(
    private val issueDao: IIssueDao,
    private val commitDao: ICommitDao
) : EntityMapper<IssueCommitConnection, IssueCommitConnectionEntity> {

    /**
     * Converts a domain IssueCommitConnection to a SQL IssueCommitConnectionEntity
     */
    override fun toEntity(domain: IssueCommitConnection): IssueCommitConnectionEntity {
        return IssueCommitConnectionEntity(
            id = domain.id,
            issueId = domain.from.id ?: throw IllegalStateException("Issue ID cannot be null"),
            commitId = domain.to.id ?: throw IllegalStateException("Commit ID cannot be null")
        )
    }

    /**
     * Converts a SQL IssueCommitConnectionEntity to a domain IssueCommitConnection
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: IssueCommitConnectionEntity): IssueCommitConnection {
        val issue = issueDao.findById(entity.issueId)
            ?: throw IllegalStateException("Issue with ID ${entity.issueId} not found")
        val commit = commitDao.findById(entity.commitId)
            ?: throw IllegalStateException("Commit with ID ${entity.commitId} not found")

        return IssueCommitConnection(
            id = entity.id,
            from = issue,
            to = commit
        )
    }

    /**
     * Converts a list of SQL IssueCommitConnectionEntity objects to a list of domain IssueCommitConnection objects
     */
    override fun toDomainList(entities: Iterable<IssueCommitConnectionEntity>): List<IssueCommitConnection> {
        return entities.map { toDomain(it) }
    }
}
