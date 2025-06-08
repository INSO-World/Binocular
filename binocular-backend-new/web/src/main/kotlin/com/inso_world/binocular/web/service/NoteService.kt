package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Note
import org.springframework.data.domain.Pageable

/**
 * Interface for NoteService.
 * Provides methods to retrieve notes and their related entities.
 */
interface NoteService {
    /**
     * Find all notes with pagination.
     *
     * @param pageable Pagination information
     * @return Iterable of notes
     */
    fun findAll(pageable: Pageable): Iterable<Note>

    /**
     * Find a note by ID.
     *
     * @param id The ID of the note to find
     * @return The note if found, null otherwise
     */
    fun findById(id: String): Note?

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
