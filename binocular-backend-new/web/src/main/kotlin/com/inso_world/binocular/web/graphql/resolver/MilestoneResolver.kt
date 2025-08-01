package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.core.service.MilestoneInfrastructurePort
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Milestone
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class MilestoneResolver(
    private val milestoneService: MilestoneInfrastructurePort,
) {
    private val logger: Logger = LoggerFactory.getLogger(MilestoneResolver::class.java)

    /**
     * Resolves the issues field for a Milestone in GraphQL.
     *
     * This method retrieves all issues associated with the given milestone.
     * If the milestone ID is null, an empty list is returned.
     *
     * @param milestone The milestone for which to retrieve issues
     * @return A list of issues associated with the milestone, or an empty list if the milestone ID is null
     */
    @SchemaMapping(typeName = "Milestone", field = "issues")
    fun issues(milestone: Milestone): List<Issue> {
        val id = milestone.id ?: return emptyList()
        logger.info("Resolving issues for milestone: $id")
        // Get all connections for this milestone and extract the issues
        return milestoneService.findIssuesByMilestoneId(id)
    }

    /**
     * Resolves the mergeRequests field for a Milestone in GraphQL.
     *
     * This method retrieves all merge requests associated with the given milestone.
     * If the milestone ID is null, an empty list is returned.
     *
     * @param milestone The milestone for which to retrieve merge requests
     * @return A list of merge requests associated with the milestone, or an empty list if the milestone ID is null
     */
    @SchemaMapping(typeName = "Milestone", field = "mergeRequests")
    fun mergeRequests(milestone: Milestone): List<MergeRequest> {
        val id = milestone.id ?: return emptyList()
        logger.info("Resolving merge requests for milestone: $id")
        // Get all connections for this milestone and extract the merge requests
        return milestoneService.findMergeRequestsByMilestoneId(id)
    }
}
