package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge

import com.inso_world.binocular.infrastructure.arangodb.model.edge.IssueNoteConnection
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Note

/**
 * Interface for IssueNoteConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
internal interface IIssueNoteConnectionDao {
    /**
     * Find all notes connected to an issue
     */
    fun findNotesByIssue(issueId: String): List<Note>

    /**
     * Find all issues connected to a note
     */
    fun findIssuesByNote(noteId: String): List<Issue>

    /**
     * Save an issue-note connection
     */
    fun save(connection: IssueNoteConnection): IssueNoteConnection

    /**
     * Delete all issue-note connections
     */
    fun deleteAll()
}
