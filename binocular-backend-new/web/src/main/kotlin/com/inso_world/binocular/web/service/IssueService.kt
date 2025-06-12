package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.persistence.model.Page
import org.springframework.data.domain.Pageable

/**
 * Interface for IssueService.
 * Provides methods to retrieve issues and their related entities.
 */
interface IssueService {
    /**
     * Find all issues with pagination.
     *
     * @param pageable Pagination information
     * @return Page of issues
     */
    fun findAll(pageable: Pageable): Page<Issue>

    /**
     * Find an issue by ID.
     *
     * @param id The ID of the issue to find
     * @return The issue if found, null otherwise
     */
    fun findById(id: String): Issue?

    /**
     * Find accounts by issue ID.
     *
     * @param issueId The ID of the issue
     * @return List of accounts associated with the issue
     */
    fun findAccountsByIssueId(issueId: String): List<Account>

    /**
     * Find commits by issue ID.
     *
     * @param issueId The ID of the issue
     * @return List of commits associated with the issue
     */
    fun findCommitsByIssueId(issueId: String): List<Commit>

    /**
     * Find milestones by issue ID.
     *
     * @param issueId The ID of the issue
     * @return List of milestones associated with the issue
     */
    fun findMilestonesByIssueId(issueId: String): List<Milestone>

    /**
     * Find notes by issue ID.
     *
     * @param issueId The ID of the issue
     * @return List of notes associated with the issue
     */
    fun findNotesByIssueId(issueId: String): List<Note>

    /**
     * Find users by issue ID.
     *
     * @param issueId The ID of the issue
     * @return List of users associated with the issue
     */
    fun findUsersByIssueId(issueId: String): List<User>
}
