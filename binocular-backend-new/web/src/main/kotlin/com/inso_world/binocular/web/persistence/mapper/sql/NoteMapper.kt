package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueNoteConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestNoteConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.INoteAccountConnectionDao
import com.inso_world.binocular.web.persistence.entity.sql.NoteEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class NoteMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    private val noteAccountConnectionDao: INoteAccountConnectionDao,
    private val issueNoteConnectionDao: IIssueNoteConnectionDao,
    private val mergeRequestNoteConnectionDao: IMergeRequestNoteConnectionDao
) : EntityMapper<Note, NoteEntity> {

    /**
     * Converts a domain Note to a SQL NoteEntity
     */
    override fun toEntity(domain: Note): NoteEntity {
        return NoteEntity(
            id = domain.id,
            body = domain.body,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            system = domain.system,
            resolvable = domain.resolvable,
            confidential = domain.confidential,
            internal = domain.internal,
            imported = domain.imported,
            importedFrom = domain.importedFrom
            // Note: Relationships are not directly mapped in SQL entity
        )
    }

    /**
     * Converts a SQL NoteEntity to a domain Note
     * 
     * Uses lazy loading proxies for relationships, which will only be loaded
     * when accessed. This provides a consistent API regardless of the database
     * implementation and avoids the N+1 query problem.
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: NoteEntity): Note {
        val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")

        return Note(
            id = id,
            body = entity.body,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            system = entity.system,
            resolvable = entity.resolvable,
            confidential = entity.confidential,
            internal = entity.internal,
            imported = entity.imported,
            importedFrom = entity.importedFrom,
            // Create lazy-loaded proxies for relationships that will load data from DAOs when accessed
            accounts = proxyFactory.createLazyList { noteAccountConnectionDao.findAccountsByNote(id) },
            issues = proxyFactory.createLazyList { issueNoteConnectionDao.findIssuesByNote(id) },
            mergeRequests = proxyFactory.createLazyList { mergeRequestNoteConnectionDao.findMergeRequestsByNote(id) }
        )
    }

    /**
     * Converts a list of SQL NoteEntity objects to a list of domain Note objects
     */
    override fun toDomainList(entities: Iterable<NoteEntity>): List<Note> {
        return entities.map { toDomain(it) }
    }
}
