package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.entity.edge.domain.MergeRequestNoteConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestNoteConnectionDao
import com.inso_world.binocular.web.persistence.entity.arangodb.MergeRequestNoteConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.arangodb.MergeRequestMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.NoteMapper
import com.inso_world.binocular.web.persistence.repository.arangodb.MergeRequestRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.NoteRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.MergeRequestNoteConnectionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IMergeRequestNoteConnectionDao.
 * 
 * This class adapts the existing MergeRequestNoteConnectionRepository to work with
 * the new domain model and entity structure.
 */
@Repository
@Profile("nosql", "arangodb")
class MergeRequestNoteConnectionDao @Autowired constructor(
    private val repository: MergeRequestNoteConnectionRepository,
    private val mergeRequestRepository: MergeRequestRepository,
    private val noteRepository: NoteRepository,
    private val mergeRequestMapper: MergeRequestMapper,
    private val noteMapper: NoteMapper
) : IMergeRequestNoteConnectionDao {

    /**
     * Find all notes connected to a merge request
     */
    override fun findNotesByMergeRequest(mergeRequestId: String): List<Note> {
        val noteEntities = repository.findNotesByMergeRequest(mergeRequestId)
        return noteEntities.map { noteMapper.toDomain(it) }
    }

    /**
     * Find all merge requests connected to a note
     */
    override fun findMergeRequestsByNote(noteId: String): List<MergeRequest> {
        val mergeRequestEntities = repository.findMergeRequestsByNote(noteId)
        return mergeRequestEntities.map { mergeRequestMapper.toDomain(it) }
    }

    /**
     * Save a merge request-note connection
     */
    override fun save(connection: MergeRequestNoteConnection): MergeRequestNoteConnection {
        // Get the merge request and note entities from their repositories
        val mergeRequestEntity = mergeRequestRepository.findById(connection.from.id!!).orElseThrow { 
            IllegalArgumentException("MergeRequest with ID ${connection.from.id} not found") 
        }
        val noteEntity = noteRepository.findById(connection.to.id!!).orElseThrow { 
            IllegalArgumentException("Note with ID ${connection.to.id} not found") 
        }

        // Convert domain model to the entity format
        val entity = MergeRequestNoteConnectionEntity(
            id = connection.id,
            from = mergeRequestEntity,
            to = noteEntity
        )

        // Save using the repository
        val savedEntity = repository.save(entity)

        // Convert back to domain model
        return MergeRequestNoteConnection(
            id = savedEntity.id,
            from = mergeRequestMapper.toDomain(savedEntity.from),
            to = noteMapper.toDomain(savedEntity.to)
        )
    }

    /**
     * Delete all merge request-note connections
     */
    override fun deleteAll() {
        repository.deleteAll()
    }
}
