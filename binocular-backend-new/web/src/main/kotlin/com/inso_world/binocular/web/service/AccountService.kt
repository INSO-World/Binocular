package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.persistence.model.Page
import org.springframework.data.domain.Pageable

/**
 * Interface for AccountService.
 * Provides methods to retrieve accounts and their related entities.
 */
interface AccountService {
    /**
     * Find all accounts with pagination.
     *
     * @param pageable Pagination information
     * @return Page of accounts
     */
    fun findAll(pageable: Pageable): Page<Account>

    /**
     * Find an account by ID.
     *
     * @param id The ID of the account to find
     * @return The account if found, null otherwise
     */
    fun findById(id: String): Account?

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
