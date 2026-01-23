package com.inso_world.binocular.core.service

import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Milestone
import com.inso_world.binocular.model.Note
import com.inso_world.binocular.model.User

/**
 * Interface for IssueService.
 * Provides methods to retrieve issues and their related entities.
 *
 * @deprecated Use [ProjectInfrastructurePort] instead. Issue is part of the Project aggregate
 *             and should be accessed through its aggregate root.
 */
@Deprecated(
    message = "Use ProjectInfrastructurePort instead. Issue is part of the Project aggregate.",
    replaceWith = ReplaceWith("ProjectInfrastructurePort"),
    level = DeprecationLevel.WARNING
)
interface IssueInfrastructurePort : BinocularInfrastructurePort<Issue, Issue.Id> {
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
