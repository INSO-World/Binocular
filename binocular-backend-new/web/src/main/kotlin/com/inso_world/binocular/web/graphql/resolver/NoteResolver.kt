package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueNoteConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.MergeRequestNoteConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.NoteAccountConnectionRepository
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class NoteResolver(
    private val noteAccountConnectionRepository: NoteAccountConnectionRepository,
    private val issueNoteConnectionRepository: IssueNoteConnectionRepository,
    private val mergeRequestNoteConnectionRepository: MergeRequestNoteConnectionRepository
) {
    @SchemaMapping(typeName = "Note", field = "accounts")
    fun accounts(note: Note): List<Account> {
        val id = note.id ?: return emptyList()
        // Get all connections for this note and extract the accounts
        return noteAccountConnectionRepository.findAccountsByNote(id)
    }

    @SchemaMapping(typeName = "Note", field = "issues")
    fun issues(note: Note): List<Issue> {
        val id = note.id ?: return emptyList()
        // Get all connections for this note and extract the issues
        return issueNoteConnectionRepository.findIssuesByNote(id)
    }

    @SchemaMapping(typeName = "Note", field = "mergeRequests")
    fun mergeRequests(note: Note): List<MergeRequest> {
        val id = note.id ?: return emptyList()
        // Get all connections for this note and extract the merge requests
        return mergeRequestNoteConnectionRepository.findMergeRequestsByNote(id)
    }
}
