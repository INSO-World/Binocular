package com.inso_world.binocular.web.persistence.dao.interfaces

import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.entity.edge.domain.MergeRequestNoteConnection

/**
 * Interface for MergeRequestNoteConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
interface IMergeRequestNoteConnectionDao {
    
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
