package com.inso_world.binocular.web.service.interfaces

import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Milestone
import org.springframework.data.domain.Pageable

/**
 * Interface for MilestoneService.
 * Provides methods to retrieve milestones and their related entities.
 */
interface MilestoneServiceInterface {
    /**
     * Find all milestones with pagination.
     *
     * @param pageable Pagination information
     * @return Iterable of milestones
     */
    fun findAll(pageable: Pageable): Iterable<Milestone>

    /**
     * Find a milestone by ID.
     *
     * @param id The ID of the milestone to find
     * @return The milestone if found, null otherwise
     */
    fun findById(id: String): Milestone?

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
