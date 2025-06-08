package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueAccountConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.MergeRequestAccountConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.NoteAccountConnectionRepository
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class AccountResolver(
    private val issueAccountConnectionRepository: IssueAccountConnectionRepository,
    private val mergeRequestAccountConnectionRepository: MergeRequestAccountConnectionRepository,
    private val noteAccountConnectionRepository: NoteAccountConnectionRepository
) {
    @SchemaMapping(typeName = "Account", field = "issues")
    fun issues(account: Account): List<Issue> {
        val id = account.id ?: return emptyList()
        // Get all connections for this account and extract the issues
        return issueAccountConnectionRepository.findIssuesByAccount(id)
    }

    @SchemaMapping(typeName = "Account", field = "mergeRequests")
    fun mergeRequests(account: Account): List<MergeRequest> {
        val id = account.id ?: return emptyList()
        // Get all connections for this account and extract the merge requests
        return mergeRequestAccountConnectionRepository.findMergeRequestsByAccount(id)
    }

    @SchemaMapping(typeName = "Account", field = "notes")
    fun notes(account: Account): List<Note> {
        val id = account.id ?: return emptyList()
        // Get all connections for this account and extract the notes
        return noteAccountConnectionRepository.findNotesByAccount(id)
    }
}
