package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.core.service.IssueInfrastructurePort
import com.inso_world.binocular.model.enums.IssueAccountRole
import com.inso_world.binocular.web.graphql.mapper.GraphQlMapper
import com.inso_world.binocular.web.graphql.model.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class IssueResolver(
    private val issueService: IssueInfrastructurePort,
    @Autowired private val mapper: GraphQlMapper,
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
    fun accounts(issue: IssueDto): List<AccountDto> {
        val id = issue.id ?: return emptyList()
        logger.info("Resolving accounts for issue: $id")
        // Get all connections for this issue and extract the accounts
        return issueService.findAccountsByIssueId(id).map { mapper.toDto(it) }
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
    fun commits(issue: IssueDto): List<CommitDto> {
        val id = issue.id ?: return emptyList()
        logger.info("Resolving commits for issue: $id")
        // Get all connections for this issue and extract the commits
        return issueService.findCommitsByIssueId(id).map { mapper.toDto(it) }
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
    fun milestones(issue: IssueDto): List<MilestoneDto> {
        val id = issue.id ?: return emptyList()
        logger.info("Resolving milestones for issue: $id")
        // Get all connections for this issue and extract the milestones
        return issueService.findMilestonesByIssueId(id).map { mapper.toDto(it) }
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
    fun notes(issue: IssueDto): List<NoteDto> {
        val id = issue.id ?: return emptyList()
        logger.info("Resolving notes for issue: $id")
        // Get all connections for this issue and extract the notes
        return issueService.findNotesByIssueId(id).map { mapper.toDto(it) }
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
    fun users(issue: IssueDto): List<UserDto> {
        val id = issue.id ?: return emptyList()
        logger.info("Resolving users for issue: $id")
        // Get all connections for this issue and extract the users
        return issueService.findUsersByIssueId(id).map { mapper.toDto(it) }
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
    fun author(issue: IssueDto): AccountDto? {
        val id = issue.id ?: return null
        logger.info("Resolving author for issue: $id")
        return issueService.findAccountsByIssueId(id, IssueAccountRole.AUTHOR).firstOrNull()?.let { mapper.toDto(it) }
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
    fun assignee(issue: IssueDto): AccountDto? {
        val id = issue.id ?: return null
        logger.info("Resolving assignee for issue: $id")
        return issueService.findAccountsByIssueId(id, IssueAccountRole.ASSIGNEE).firstOrNull()?.let { mapper.toDto(it) }
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
    fun assignees(issue: IssueDto): List<AccountDto> {
        val id = issue.id ?: return emptyList()
        logger.info("Resolving assignees for issue: $id")
        return issueService.findAccountsByIssueId(id, IssueAccountRole.ASSIGNEES).map { mapper.toDto(it) }
    }

}
