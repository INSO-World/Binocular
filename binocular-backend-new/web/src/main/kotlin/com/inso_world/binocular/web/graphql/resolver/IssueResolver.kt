package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.service.IssueService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class IssueResolver(
    private val issueService: IssueService
) {
    private val logger: Logger = LoggerFactory.getLogger(IssueResolver::class.java)
    @SchemaMapping(typeName = "Issue", field = "accounts")
    fun accounts(issue: Issue): List<Account> {
        val id = issue.id ?: return emptyList()
        logger.info("Resolving accounts for issue: $id")
        // Get all connections for this issue and extract the accounts
        return issueService.findAccountsByIssueId(id)
    }

    @SchemaMapping(typeName = "Issue", field = "commits")
    fun commits(issue: Issue): List<Commit> {
        val id = issue.id ?: return emptyList()
        logger.info("Resolving commits for issue: $id")
        // Get all connections for this issue and extract the commits
        return issueService.findCommitsByIssueId(id)
    }

    @SchemaMapping(typeName = "Issue", field = "milestones")
    fun milestones(issue: Issue): List<Milestone> {
        val id = issue.id ?: return emptyList()
        logger.info("Resolving milestones for issue: $id")
        // Get all connections for this issue and extract the milestones
        return issueService.findMilestonesByIssueId(id)
    }

    @SchemaMapping(typeName = "Issue", field = "notes")
    fun notes(issue: Issue): List<Note> {
        val id = issue.id ?: return emptyList()
        logger.info("Resolving notes for issue: $id")
        // Get all connections for this issue and extract the notes
        return issueService.findNotesByIssueId(id)
    }

    @SchemaMapping(typeName = "Issue", field = "users")
    fun users(issue: Issue): List<User> {
        val id = issue.id ?: return emptyList()
        logger.info("Resolving users for issue: $id")
        // Get all connections for this issue and extract the users
        return issueService.findUsersByIssueId(id)
    }
}
