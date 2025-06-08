package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.MergeRequestAccountConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.MergeRequestMilestoneConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.MergeRequestNoteConnectionRepository
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class MergeRequestResolver(
    private val mergeRequestAccountConnectionRepository: MergeRequestAccountConnectionRepository,
    private val mergeRequestMilestoneConnectionRepository: MergeRequestMilestoneConnectionRepository,
    private val mergeRequestNoteConnectionRepository: MergeRequestNoteConnectionRepository
) {
    @SchemaMapping(typeName = "MergeRequest", field = "accounts")
    fun accounts(mergeRequest: MergeRequest): List<Account> {
        val id = mergeRequest.id ?: return emptyList()
        // Get all connections for this merge request and extract the accounts
        return mergeRequestAccountConnectionRepository.findAccountsByMergeRequest(id)
    }

    @SchemaMapping(typeName = "MergeRequest", field = "milestones")
    fun milestones(mergeRequest: MergeRequest): List<Milestone> {
        val id = mergeRequest.id ?: return emptyList()
        // Get all connections for this merge request and extract the milestones
        return mergeRequestMilestoneConnectionRepository.findMilestonesByMergeRequest(id)
    }

    @SchemaMapping(typeName = "MergeRequest", field = "notes")
    fun notes(mergeRequest: MergeRequest): List<Note> {
        val id = mergeRequest.id ?: return emptyList()
        // Get all connections for this merge request and extract the notes
        return mergeRequestNoteConnectionRepository.findNotesByMergeRequest(id)
    }
}
