package com.inso_world.binocular.core.service

import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Milestone
import com.inso_world.binocular.model.Note

/**
 * Interface for MergeRequestService.
 * Provides methods to retrieve merge requests and their related entities.
 */
interface MergeRequestInfrastructurePort : BinocularInfrastructurePort<MergeRequest> {
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
