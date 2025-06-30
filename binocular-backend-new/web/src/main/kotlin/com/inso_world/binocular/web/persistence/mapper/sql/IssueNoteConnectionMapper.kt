package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.edge.domain.IssueNoteConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueDao
import com.inso_world.binocular.web.persistence.dao.interfaces.INoteDao
import com.inso_world.binocular.web.persistence.entity.sql.IssueNoteConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class IssueNoteConnectionMapper @Autowired constructor(
    private val issueDao: IIssueDao,
    private val noteDao: INoteDao
) : EntityMapper<IssueNoteConnection, IssueNoteConnectionEntity> {

    /**
     * Converts a domain IssueNoteConnection to a SQL IssueNoteConnectionEntity
     */
    override fun toEntity(domain: IssueNoteConnection): IssueNoteConnectionEntity {
        return IssueNoteConnectionEntity(
            id = domain.id,
            issueId = domain.from.id ?: throw IllegalStateException("Issue ID cannot be null"),
            noteId = domain.to.id ?: throw IllegalStateException("Note ID cannot be null")
        )
    }

    /**
     * Converts a SQL IssueNoteConnectionEntity to a domain IssueNoteConnection
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: IssueNoteConnectionEntity): IssueNoteConnection {
        val issue = issueDao.findById(entity.issueId)
            ?: throw IllegalStateException("Issue with ID ${entity.issueId} not found")
        val note = noteDao.findById(entity.noteId)
            ?: throw IllegalStateException("Note with ID ${entity.noteId} not found")

        return IssueNoteConnection(
            id = entity.id,
            from = issue,
            to = note
        )
    }

    /**
     * Converts a list of SQL IssueNoteConnectionEntity objects to a list of domain IssueNoteConnection objects
     */
    override fun toDomainList(entities: Iterable<IssueNoteConnectionEntity>): List<IssueNoteConnection> {
        return entities.map { toDomain(it) }
    }
}
