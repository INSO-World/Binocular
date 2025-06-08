package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueMilestoneConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.MergeRequestMilestoneConnectionRepository
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class MilestoneResolver(
    private val issueMilestoneConnectionRepository: IssueMilestoneConnectionRepository,
    private val mergeRequestMilestoneConnectionRepository: MergeRequestMilestoneConnectionRepository
) {
    @SchemaMapping(typeName = "Milestone", field = "issues")
    fun issues(milestone: Milestone): List<Issue> {
        val id = milestone.id ?: return emptyList()
        // Get all connections for this milestone and extract the issues
        return issueMilestoneConnectionRepository.findIssuesByMilestone(id)
    }

    @SchemaMapping(typeName = "Milestone", field = "mergeRequests")
    fun mergeRequests(milestone: Milestone): List<MergeRequest> {
        val id = milestone.id ?: return emptyList()
        // Get all connections for this milestone and extract the merge requests
        return mergeRequestMilestoneConnectionRepository.findMergeRequestsByMilestone(id)
    }
}
