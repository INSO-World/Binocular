package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge

import com.inso_world.binocular.infrastructure.arangodb.model.edge.NoteAccountConnection
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Note

/**
 * Interface for NoteAccountConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
internal interface INoteAccountConnectionDao {
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
