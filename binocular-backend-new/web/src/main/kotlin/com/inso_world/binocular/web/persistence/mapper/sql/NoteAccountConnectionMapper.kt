package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.edge.domain.NoteAccountConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IAccountDao
import com.inso_world.binocular.web.persistence.dao.interfaces.INoteDao
import com.inso_world.binocular.web.persistence.entity.sql.NoteAccountConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class NoteAccountConnectionMapper @Autowired constructor(
    private val noteDao: INoteDao,
    private val accountDao: IAccountDao
) : EntityMapper<NoteAccountConnection, NoteAccountConnectionEntity> {

    /**
     * Converts a domain NoteAccountConnection to a SQL NoteAccountConnectionEntity
     */
    override fun toEntity(domain: NoteAccountConnection): NoteAccountConnectionEntity {
        return NoteAccountConnectionEntity(
            id = domain.id,
            noteId = domain.from.id ?: throw IllegalStateException("Note ID cannot be null"),
            accountId = domain.to.id ?: throw IllegalStateException("Account ID cannot be null")
        )
    }

    /**
     * Converts a SQL NoteAccountConnectionEntity to a domain NoteAccountConnection
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: NoteAccountConnectionEntity): NoteAccountConnection {
        val note = noteDao.findById(entity.noteId)
            ?: throw IllegalStateException("Note with ID ${entity.noteId} not found")
        val account = accountDao.findById(entity.accountId)
            ?: throw IllegalStateException("Account with ID ${entity.accountId} not found")

        return NoteAccountConnection(
            id = entity.id,
            from = note,
            to = account
        )
    }

    /**
     * Converts a list of SQL NoteAccountConnectionEntity objects to a list of domain NoteAccountConnection objects
     */
    override fun toDomainList(entities: Iterable<NoteAccountConnectionEntity>): List<NoteAccountConnection> {
        return entities.map { toDomain(it) }
    }
}
