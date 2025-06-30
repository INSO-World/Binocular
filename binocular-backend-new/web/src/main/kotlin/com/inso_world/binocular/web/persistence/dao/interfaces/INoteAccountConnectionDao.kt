package com.inso_world.binocular.web.persistence.dao.interfaces

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.entity.edge.domain.NoteAccountConnection

/**
 * Interface for NoteAccountConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
interface INoteAccountConnectionDao {
    
    /**
     * Find all accounts connected to a note
     */
    fun findAccountsByNote(noteId: String): List<Account>
    
    /**
     * Find all notes connected to an account
     */
    fun findNotesByAccount(accountId: String): List<Note>
    
    /**
     * Save a note-account connection
     */
    fun save(connection: NoteAccountConnection): NoteAccountConnection
    
    /**
     * Delete all note-account connections
     */
    fun deleteAll()
}
