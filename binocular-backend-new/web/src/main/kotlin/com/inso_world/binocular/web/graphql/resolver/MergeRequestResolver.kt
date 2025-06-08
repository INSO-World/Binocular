package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.service.MergeRequestService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class MergeRequestResolver(
    private val mergeRequestService: MergeRequestService
) {
    private val logger: Logger = LoggerFactory.getLogger(MergeRequestResolver::class.java)
    @SchemaMapping(typeName = "MergeRequest", field = "accounts")
    fun accounts(mergeRequest: MergeRequest): List<Account> {
        val id = mergeRequest.id ?: return emptyList()
        logger.info("Resolving accounts for merge request: $id")
        // Get all connections for this merge request and extract the accounts
        return mergeRequestService.findAccountsByMergeRequestId(id)
    }

    @SchemaMapping(typeName = "MergeRequest", field = "milestones")
    fun milestones(mergeRequest: MergeRequest): List<Milestone> {
        val id = mergeRequest.id ?: return emptyList()
        logger.info("Resolving milestones for merge request: $id")
        // Get all connections for this merge request and extract the milestones
        return mergeRequestService.findMilestonesByMergeRequestId(id)
    }

    @SchemaMapping(typeName = "MergeRequest", field = "notes")
    fun notes(mergeRequest: MergeRequest): List<Note> {
        val id = mergeRequest.id ?: return emptyList()
        logger.info("Resolving notes for merge request: $id")
        // Get all connections for this merge request and extract the notes
        return mergeRequestService.findNotesByMergeRequestId(id)
    }
}
