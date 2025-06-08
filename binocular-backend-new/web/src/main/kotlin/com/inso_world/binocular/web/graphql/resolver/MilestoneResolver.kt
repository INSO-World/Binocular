package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.service.MilestoneService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class MilestoneResolver(
    private val milestoneService: MilestoneService
) {
    private val logger: Logger = LoggerFactory.getLogger(MilestoneResolver::class.java)
    @SchemaMapping(typeName = "Milestone", field = "issues")
    fun issues(milestone: Milestone): List<Issue> {
        val id = milestone.id ?: return emptyList()
        logger.info("Resolving issues for milestone: $id")
        // Get all connections for this milestone and extract the issues
        return milestoneService.findIssuesByMilestoneId(id)
    }

    @SchemaMapping(typeName = "Milestone", field = "mergeRequests")
    fun mergeRequests(milestone: Milestone): List<MergeRequest> {
        val id = milestone.id ?: return emptyList()
        logger.info("Resolving merge requests for milestone: $id")
        // Get all connections for this milestone and extract the merge requests
        return milestoneService.findMergeRequestsByMilestoneId(id)
    }
}
