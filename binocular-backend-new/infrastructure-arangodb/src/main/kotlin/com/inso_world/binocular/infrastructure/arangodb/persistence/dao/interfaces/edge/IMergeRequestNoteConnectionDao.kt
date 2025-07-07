package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge

import com.inso_world.binocular.infrastructure.arangodb.model.edge.MergeRequestNoteConnection
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Note

/**
 * Interface for MergeRequestNoteConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
internal interface IMergeRequestNoteConnectionDao {
    /**
     * Find all notes connected to a merge request
     */
    fun findNotesByMergeRequest(mergeRequestId: String): List<Note>

    /**
     * Find all merge requests connected to a note
     */
    fun findMergeRequestsByNote(noteId: String): List<MergeRequest>

    /**
     * Save a merge request-note connection
     */
    fun save(connection: MergeRequestNoteConnection): MergeRequestNoteConnection

    /**
     * Delete all merge request-note connections
     */
    fun deleteAll()
}
