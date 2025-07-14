package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.NoteInfrastructurePort
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IIssueNoteConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IMergeRequestNoteConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.INoteAccountConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.INoteDao
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Note
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

/**
 * Implementation of the NoteService interface.
 * This service is database-agnostic and works with both ArangoDB and SQL implementations.
 */
@Service
class NoteInfrastructurePortImpl : NoteInfrastructurePort {
    @Autowired private lateinit var noteDao: INoteDao

    @Autowired private lateinit var noteAccountConnectionRepository: INoteAccountConnectionDao

    @Autowired private lateinit var issueNoteConnectionRepository: IIssueNoteConnectionDao

    @Autowired private lateinit var mergeRequestNoteConnectionRepository: IMergeRequestNoteConnectionDao
    var logger: Logger = LoggerFactory.getLogger(NoteInfrastructurePortImpl::class.java)

    override fun findAll(pageable: Pageable): Page<Note> {
        logger.trace("Getting all notes with pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}")
        return noteDao.findAll(pageable)
    }

    override fun findById(id: String): Note? {
        logger.trace("Getting note by id: $id")
        return noteDao.findById(id)
    }

    override fun findAccountsByNoteId(noteId: String): List<Account> {
        logger.trace("Getting accounts for note: $noteId")
        return noteAccountConnectionRepository.findAccountsByNote(noteId)
    }

    override fun findIssuesByNoteId(noteId: String): List<Issue> {
        logger.trace("Getting issues for note: $noteId")
        return issueNoteConnectionRepository.findIssuesByNote(noteId)
    }

    override fun findMergeRequestsByNoteId(noteId: String): List<MergeRequest> {
        logger.trace("Getting merge requests for note: $noteId")
        return mergeRequestNoteConnectionRepository.findMergeRequestsByNote(noteId)
    }

    override fun findAll(): Iterable<Note> = noteDao.findAll()

    override fun save(entity: Note): Note = noteDao.save(entity)

    override fun saveAll(entities: Collection<Note>): Iterable<Note> = noteDao.saveAll(entities)

    override fun delete(entity: Note) = noteDao.delete(entity)

    override fun update(entity: Note): Note {
        TODO("Not yet implemented")
    }

    override fun updateAndFlush(entity: Note): Note {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: String) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        this.noteDao.deleteAll()
    }
}
