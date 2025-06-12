package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.service.NoteService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class NoteResolver(
    private val noteService: NoteService
) {
    private val logger: Logger = LoggerFactory.getLogger(NoteResolver::class.java)
    /**
     * Resolves the accounts field for a Note in GraphQL.
     * 
     * This method retrieves all accounts associated with the given note.
     * If the note ID is null, an empty list is returned.
     * 
     * @param note The note for which to retrieve accounts
     * @return A list of accounts associated with the note, or an empty list if the note ID is null
     */
    @SchemaMapping(typeName = "Note", field = "accounts")
    fun accounts(note: Note): List<Account> {
        val id = note.id ?: return emptyList()
        logger.info("Resolving accounts for note: $id")
        // Get all connections for this note and extract the accounts
        return noteService.findAccountsByNoteId(id)
    }

    /**
     * Resolves the issues field for a Note in GraphQL.
     * 
     * This method retrieves all issues associated with the given note.
     * If the note ID is null, an empty list is returned.
     * 
     * @param note The note for which to retrieve issues
     * @return A list of issues associated with the note, or an empty list if the note ID is null
     */
    @SchemaMapping(typeName = "Note", field = "issues")
    fun issues(note: Note): List<Issue> {
        val id = note.id ?: return emptyList()
        logger.info("Resolving issues for note: $id")
        // Get all connections for this note and extract the issues
        return noteService.findIssuesByNoteId(id)
    }

    /**
     * Resolves the mergeRequests field for a Note in GraphQL.
     * 
     * This method retrieves all merge requests associated with the given note.
     * If the note ID is null, an empty list is returned.
     * 
     * @param note The note for which to retrieve merge requests
     * @return A list of merge requests associated with the note, or an empty list if the note ID is null
     */
    @SchemaMapping(typeName = "Note", field = "mergeRequests")
    fun mergeRequests(note: Note): List<MergeRequest> {
        val id = note.id ?: return emptyList()
        logger.info("Resolving merge requests for note: $id")
        // Get all connections for this note and extract the merge requests
        return noteService.findMergeRequestsByNoteId(id)
    }
}
