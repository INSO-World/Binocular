package com.inso_world.binocular.web.service.interfaces

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.Milestone
import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Note
import org.springframework.data.domain.Pageable

/**
 * Interface for MergeRequestService.
 * Provides methods to retrieve merge requests and their related entities.
 */
interface MergeRequestServiceInterface {
    /**
     * Find all merge requests with pagination.
     *
     * @param pageable Pagination information
     * @return Iterable of merge requests
     */
    fun findAll(pageable: Pageable): Iterable<MergeRequest>

    /**
     * Find a merge request by ID.
     *
     * @param id The ID of the merge request to find
     * @return The merge request if found, null otherwise
     */
    fun findById(id: String): MergeRequest?

    /**
     * Find accounts by merge request ID.
     *
     * @param mergeRequestId The ID of the merge request
     * @return List of accounts associated with the merge request
     */
    fun findAccountsByMergeRequestId(mergeRequestId: String): List<Account>

    /**
     * Find milestones by merge request ID.
     *
     * @param mergeRequestId The ID of the merge request
     * @return List of milestones associated with the merge request
     */
    fun findMilestonesByMergeRequestId(mergeRequestId: String): List<Milestone>

    /**
     * Find notes by merge request ID.
     *
     * @param mergeRequestId The ID of the merge request
     * @return List of notes associated with the merge request
     */
    fun findNotesByMergeRequestId(mergeRequestId: String): List<Note>
}
