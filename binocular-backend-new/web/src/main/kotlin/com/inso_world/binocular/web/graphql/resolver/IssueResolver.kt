package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.core.service.IssueInfrastructurePort
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.enums.IssueAccountRole
import com.inso_world.binocular.model.Milestone
import com.inso_world.binocular.model.Note
import com.inso_world.binocular.model.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class IssueResolver(
    private val issueService: IssueInfrastructurePort,
) {
    private val logger: Logger = LoggerFactory.getLogger(IssueResolver::class.java)

    /**
     * Resolves the accounts field for an Issue in GraphQL.
     *
     * This method retrieves all accounts associated with the given issue.
     * If the issue ID is null, an empty list is returned.
     *
     * @param issue The issue for which to retrieve accounts
     * @return A list of accounts associated with the issue, or an empty list if the issue ID is null
     */
    @SchemaMapping(typeName = "Issue", field = "accounts")
    fun accounts(issue: Issue): List<Account> {
        val id = issue.id ?: return emptyList()
        logger.info("Resolving accounts for issue: $id")
        // Get all connections for this issue and extract the accounts
        return issueService.findAccountsByIssueId(id)
    }

    /**
     * Resolves the commits field for an Issue in GraphQL.
     *
     * This method retrieves all commits associated with the given issue.
     * If the issue ID is null, an empty list is returned.
     *
     * @param issue The issue for which to retrieve commits
     * @return A list of commits associated with the issue, or an empty list if the issue ID is null
     */
    @SchemaMapping(typeName = "Issue", field = "commits")
    fun commits(issue: Issue): List<Commit> {
        val id = issue.id ?: return emptyList()
        logger.info("Resolving commits for issue: $id")
        // Get all connections for this issue and extract the commits
        return issueService.findCommitsByIssueId(id)
    }

    /**
     * Resolves the milestones field for an Issue in GraphQL.
     *
     * This method retrieves all milestones associated with the given issue.
     * If the issue ID is null, an empty list is returned.
     *
     * @param issue The issue for which to retrieve milestones
     * @return A list of milestones associated with the issue, or an empty list if the issue ID is null
     */
    @SchemaMapping(typeName = "Issue", field = "milestones")
    fun milestones(issue: Issue): List<Milestone> {
        val id = issue.id ?: return emptyList()
        logger.info("Resolving milestones for issue: $id")
        // Get all connections for this issue and extract the milestones
        return issueService.findMilestonesByIssueId(id)
    }

    /**
     * Resolves the notes field for an Issue in GraphQL.
     *
     * This method retrieves all notes associated with the given issue.
     * If the issue ID is null, an empty list is returned.
     *
     * @param issue The issue for which to retrieve notes
     * @return A list of notes associated with the issue, or an empty list if the issue ID is null
     */
    @SchemaMapping(typeName = "Issue", field = "notes")
    fun notes(issue: Issue): List<Note> {
        val id = issue.id ?: return emptyList()
        logger.info("Resolving notes for issue: $id")
        // Get all connections for this issue and extract the notes
        return issueService.findNotesByIssueId(id)
    }

    /**
     * Resolves the users field for an Issue in GraphQL.
     *
     * This method retrieves all users associated with the given issue.
     * If the issue ID is null, an empty list is returned.
     *
     * @param issue The issue for which to retrieve users
     * @return A list of users associated with the issue, or an empty list if the issue ID is null
     */
    @SchemaMapping(typeName = "Issue", field = "users")
    fun users(issue: Issue): List<User> {
        val id = issue.id ?: return emptyList()
        logger.info("Resolving users for issue: $id")
        // Get all connections for this issue and extract the users
        return issueService.findUsersByIssueId(id)
    }

    /**
     * Resolves the author field for an Issue in GraphQL.
     *
     * This method retrieves the account associated with the issue in the AUTHOR role.
     * If the issue ID is null or no author is associated, null is returned.
     *
     * @param issue The issue for which to retrieve the author
     * @return The author account associated with the issue, or null if none exists
     */
    @SchemaMapping(typeName = "Issue", field = "author")
    fun author(issue: Issue): Account? {
        val id = issue.id ?: return null
        logger.info("Resolving author for issue: $id")
        return issueService.findAccountsByIssueId(id, IssueAccountRole.AUTHOR).firstOrNull()
    }

    /**
     * Resolves the assignee field for an Issue in GraphQL.
     *
     * This method retrieves the account associated with the issue in the ASSIGNEE role.
     * If the issue ID is null or no assignee is associated, null is returned.
     *
     * @param issue The issue for which to retrieve the assignee
     * @return The assignee account associated with the issue, or null if none exists
     */
    @SchemaMapping(typeName = "Issue", field = "assignee")
    fun assignee(issue: Issue): Account? {
        val id = issue.id ?: return null
        logger.info("Resolving assignee for issue: $id")
        return issueService.findAccountsByIssueId(id, IssueAccountRole.ASSIGNEE).firstOrNull()
    }

    /**
     * Resolves the assignees field for an Issue in GraphQL.
     *
     * This method retrieves all accounts associated with the issue in the ASSIGNEES role.
     * If the issue ID is null, an empty list is returned.
     *
     * @param issue The issue for which to retrieve assignees
     * @return A list of assignee accounts associated with the issue, or an empty list if the issue ID is null
     */
    @SchemaMapping(typeName = "Issue", field = "assignees")
    fun assignees(issue: Issue): List<Account> {
        val id = issue.id ?: return emptyList()
        logger.info("Resolving assignees for issue: $id")
        return issueService.findAccountsByIssueId(id, IssueAccountRole.ASSIGNEES)
    }

}
