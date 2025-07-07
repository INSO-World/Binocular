package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.core.service.MergeRequestInfrastructurePort
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Milestone
import com.inso_world.binocular.model.Note
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class MergeRequestResolver(
    private val mergeRequestService: MergeRequestInfrastructurePort,
) {
    private val logger: Logger = LoggerFactory.getLogger(MergeRequestResolver::class.java)

    /**
     * Resolves the accounts field for a MergeRequest in GraphQL.
     *
     * This method retrieves all accounts associated with the given merge request.
     * If the merge request ID is null, an empty list is returned.
     *
     * @param mergeRequest The merge request for which to retrieve accounts
     * @return A list of accounts associated with the merge request, or an empty list if the merge request ID is null
     */
    @SchemaMapping(typeName = "MergeRequest", field = "accounts")
    fun accounts(mergeRequest: MergeRequest): List<Account> {
        val id = mergeRequest.id ?: return emptyList()
        logger.info("Resolving accounts for merge request: $id")
        // Get all connections for this merge request and extract the accounts
        return mergeRequestService.findAccountsByMergeRequestId(id)
    }

    /**
     * Resolves the milestones field for a MergeRequest in GraphQL.
     *
     * This method retrieves all milestones associated with the given merge request.
     * If the merge request ID is null, an empty list is returned.
     *
     * @param mergeRequest The merge request for which to retrieve milestones
     * @return A list of milestones associated with the merge request, or an empty list if the merge request ID is null
     */
    @SchemaMapping(typeName = "MergeRequest", field = "milestones")
    fun milestones(mergeRequest: MergeRequest): List<Milestone> {
        val id = mergeRequest.id ?: return emptyList()
        logger.info("Resolving milestones for merge request: $id")
        // Get all connections for this merge request and extract the milestones
        return mergeRequestService.findMilestonesByMergeRequestId(id)
    }

    /**
     * Resolves the notes field for a MergeRequest in GraphQL.
     *
     * This method retrieves all notes associated with the given merge request.
     * If the merge request ID is null, an empty list is returned.
     *
     * @param mergeRequest The merge request for which to retrieve notes
     * @return A list of notes associated with the merge request, or an empty list if the merge request ID is null
     */
    @SchemaMapping(typeName = "MergeRequest", field = "notes")
    fun notes(mergeRequest: MergeRequest): List<Note> {
        val id = mergeRequest.id ?: return emptyList()
        logger.info("Resolving notes for merge request: $id")
        // Get all connections for this merge request and extract the notes
        return mergeRequestService.findNotesByMergeRequestId(id)
    }
}
