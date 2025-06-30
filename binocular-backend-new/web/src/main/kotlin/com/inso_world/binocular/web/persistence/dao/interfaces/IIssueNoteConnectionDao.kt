package com.inso_world.binocular.web.persistence.dao.interfaces

import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.entity.edge.domain.IssueNoteConnection

/**
 * Interface for IssueNoteConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
interface IIssueNoteConnectionDao {
    
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
