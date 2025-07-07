package com.inso_world.binocular.core.service

import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Milestone

/**
 * Interface for MilestoneService.
 * Provides methods to retrieve milestones and their related entities.
 */
interface MilestoneInfrastructurePort : BinocularInfrastructurePort<Milestone> {
    /**
     * Find issues by milestone ID.
     *
     * @param milestoneId The ID of the milestone
     * @return List of issues associated with the milestone
     */
    fun findIssuesByMilestoneId(milestoneId: String): List<Issue>

    /**
     * Find merge requests by milestone ID.
     *
     * @param milestoneId The ID of the milestone
     * @return List of merge requests associated with the milestone
     */
    fun findMergeRequestsByMilestoneId(milestoneId: String): List<MergeRequest>
}
