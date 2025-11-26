package com.inso_world.binocular.core.service

import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Milestone

/**
 * Interface for MilestoneService.
 * Provides methods to retrieve milestones and their related entities.
 *
 * @deprecated Use [ProjectInfrastructurePort] instead. Milestone is part of the Project aggregate
 *             and should be accessed through its aggregate root.
 */
@Deprecated(
    message = "Use ProjectInfrastructurePort instead. Milestone is part of the Project aggregate.",
    replaceWith = ReplaceWith("ProjectInfrastructurePort"),
    level = DeprecationLevel.WARNING
)
interface MilestoneInfrastructurePort : BinocularInfrastructurePort<Milestone, Milestone.Id> {
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
