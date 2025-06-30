package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.entity.edge.domain.MergeRequestNoteConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestNoteConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.INoteDao
import com.inso_world.binocular.web.persistence.entity.sql.MergeRequestEntity
import com.inso_world.binocular.web.persistence.entity.sql.NoteEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * SQL implementation of MergeRequestNoteConnectionDao that uses direct JPA relationships
 * instead of intermediate connection entities.
 */
@Repository
@Profile("sql")
@Transactional
class MergeRequestNoteConnectionDao(
    @Autowired private val mergeRequestDao: IMergeRequestDao,
    @Autowired private val noteDao: INoteDao
) : IMergeRequestNoteConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findNotesByMergeRequest(mergeRequestId: String): List<Note> {
        // Use the direct relationship between MergeRequest and Note
        val query = entityManager.createQuery(
            "SELECT mr FROM MergeRequestEntity mr WHERE mr.id = :mergeRequestId",
            MergeRequestEntity::class.java
        )
        query.setParameter("mergeRequestId", mergeRequestId)
        val mergeRequestEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return mergeRequestEntity.notes.map { noteDao.findById(it.id!!)!! }
    }

    override fun findMergeRequestsByNote(noteId: String): List<MergeRequest> {
        // Use the direct relationship between Note and MergeRequest
        val query = entityManager.createQuery(
            "SELECT n FROM NoteEntity n WHERE n.id = :noteId",
            NoteEntity::class.java
        )
        query.setParameter("noteId", noteId)
        val noteEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return noteEntity.mergeRequests.map { mergeRequestDao.findById(it.id!!)!! }
    }

    override fun save(connection: MergeRequestNoteConnection): MergeRequestNoteConnection {
        val mergeRequestId = connection.from.id ?: throw IllegalStateException("MergeRequest ID cannot be null")
        val noteId = connection.to.id ?: throw IllegalStateException("Note ID cannot be null")

        // Find the entities
        val mergeRequestEntity = entityManager.find(MergeRequestEntity::class.java, mergeRequestId)
            ?: throw IllegalStateException("MergeRequest with ID $mergeRequestId not found")
        val noteEntity = entityManager.find(NoteEntity::class.java, noteId)
            ?: throw IllegalStateException("Note with ID $noteId not found")

        // Add the relationship if it doesn't exist
        if (!mergeRequestEntity.notes.contains(noteEntity)) {
            mergeRequestEntity.notes.add(noteEntity)
            entityManager.merge(mergeRequestEntity)
        }

        // Generate a connection ID if not provided
        val connectionId = connection.id ?: UUID.randomUUID().toString()

        // Return the connection with the mergeRequest and note
        return MergeRequestNoteConnection(
            id = connectionId,
            from = mergeRequestDao.findById(mergeRequestId)!!,
            to = noteDao.findById(noteId)!!
        )
    }

    override fun deleteAll() {
        // Clear all relationships between mergeRequests and notes
        val mergeRequests = entityManager.createQuery("FROM MergeRequestEntity", MergeRequestEntity::class.java).resultList
        for (mergeRequest in mergeRequests) {
            mergeRequest.notes.clear()
            entityManager.merge(mergeRequest)
        }
    }
}
