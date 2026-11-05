package com.inso_world.binocular.core.service

import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Note

/**
 * Interface for NoteService.
 * Provides methods to retrieve notes and their related entities.
 *
 * @deprecated Use [ProjectInfrastructurePort] instead. Note is part of the Project aggregate
 *             and should be accessed through its aggregate root.
 */
@Deprecated(
    message = "Use ProjectInfrastructurePort instead. Note is part of the Project aggregate.",
    replaceWith = ReplaceWith("ProjectInfrastructurePort"),
    level = DeprecationLevel.WARNING
)
interface NoteInfrastructurePort : BinocularInfrastructurePort<Note, Note.Id> {
    /**
     * Find accounts by note ID.
     *
     * @param noteId The ID of the note
     * @return List of accounts associated with the note
     */
    fun findAccountsByNoteId(noteId: String): List<Account>

    /**
     * Find issues by note ID.
     *
     * @param noteId The ID of the note
     * @return List of issues associated with the note
     */
    fun findIssuesByNoteId(noteId: String): List<Issue>

    /**
     * Find merge requests by note ID.
     *
     * @param noteId The ID of the note
     * @return List of merge requests associated with the note
     */
    fun findMergeRequestsByNoteId(noteId: String): List<MergeRequest>
}
