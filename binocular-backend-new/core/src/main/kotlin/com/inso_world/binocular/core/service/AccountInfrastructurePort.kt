package com.inso_world.binocular.core.service

import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Note

/**
 * Interface for AccountService.
 * Provides methods to retrieve accounts and their related entities.
 */
interface AccountInfrastructurePort : BinocularInfrastructurePort<Account> {
    /**
     * Find issues by account ID.
     *
     * @param accountId The ID of the account
     * @return List of issues associated with the account
     */
    fun findIssuesByAccountId(accountId: String): List<Issue>

    /**
     * Find merge requests by account ID.
     *
     * @param accountId The ID of the account
     * @return List of merge requests associated with the account
     */
    fun findMergeRequestsByAccountId(accountId: String): List<MergeRequest>

    /**
     * Find notes by account ID.
     *
     * @param accountId The ID of the account
     * @return List of notes associated with the account
     */
    fun findNotesByAccountId(accountId: String): List<Note>
}
