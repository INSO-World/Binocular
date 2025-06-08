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
    @SchemaMapping(typeName = "Note", field = "accounts")
    fun accounts(note: Note): List<Account> {
        val id = note.id ?: return emptyList()
        logger.info("Resolving accounts for note: $id")
        // Get all connections for this note and extract the accounts
        return noteService.findAccountsByNoteId(id)
    }

    @SchemaMapping(typeName = "Note", field = "issues")
    fun issues(note: Note): List<Issue> {
        val id = note.id ?: return emptyList()
        logger.info("Resolving issues for note: $id")
        // Get all connections for this note and extract the issues
        return noteService.findIssuesByNoteId(id)
    }

    @SchemaMapping(typeName = "Note", field = "mergeRequests")
    fun mergeRequests(note: Note): List<MergeRequest> {
        val id = note.id ?: return emptyList()
        logger.info("Resolving merge requests for note: $id")
        // Get all connections for this note and extract the merge requests
        return noteService.findMergeRequestsByNoteId(id)
    }
}
