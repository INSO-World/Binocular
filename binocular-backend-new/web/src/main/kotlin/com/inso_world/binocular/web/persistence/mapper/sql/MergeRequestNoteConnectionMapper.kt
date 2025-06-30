package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.edge.domain.MergeRequestNoteConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestDao
import com.inso_world.binocular.web.persistence.dao.interfaces.INoteDao
import com.inso_world.binocular.web.persistence.entity.sql.MergeRequestNoteConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class MergeRequestNoteConnectionMapper @Autowired constructor(
    private val mergeRequestDao: IMergeRequestDao,
    private val noteDao: INoteDao
) : EntityMapper<MergeRequestNoteConnection, MergeRequestNoteConnectionEntity> {

    /**
     * Converts a domain MergeRequestNoteConnection to a SQL MergeRequestNoteConnectionEntity
     */
    override fun toEntity(domain: MergeRequestNoteConnection): MergeRequestNoteConnectionEntity {
        return MergeRequestNoteConnectionEntity(
            id = domain.id,
            mergeRequestId = domain.from.id ?: throw IllegalStateException("MergeRequest ID cannot be null"),
            noteId = domain.to.id ?: throw IllegalStateException("Note ID cannot be null")
        )
    }

    /**
     * Converts a SQL MergeRequestNoteConnectionEntity to a domain MergeRequestNoteConnection
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: MergeRequestNoteConnectionEntity): MergeRequestNoteConnection {
        val mergeRequest = mergeRequestDao.findById(entity.mergeRequestId)
            ?: throw IllegalStateException("MergeRequest with ID ${entity.mergeRequestId} not found")
        val note = noteDao.findById(entity.noteId)
            ?: throw IllegalStateException("Note with ID ${entity.noteId} not found")

        return MergeRequestNoteConnection(
            id = entity.id,
            from = mergeRequest,
            to = note
        )
    }

    /**
     * Converts a list of SQL MergeRequestNoteConnectionEntity objects to a list of domain MergeRequestNoteConnection objects
     */
    override fun toDomainList(entities: Iterable<MergeRequestNoteConnectionEntity>): List<MergeRequestNoteConnection> {
        return entities.map { toDomain(it) }
    }
}
