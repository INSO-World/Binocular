package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueAccountConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueCommitConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueMilestoneConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueNoteConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueUserConnectionRepository
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class IssueResolver(
    private val issueAccountConnectionRepository: IssueAccountConnectionRepository,
    private val issueCommitConnectionRepository: IssueCommitConnectionRepository,
    private val issueMilestoneConnectionRepository: IssueMilestoneConnectionRepository,
    private val issueNoteConnectionRepository: IssueNoteConnectionRepository,
    private val issueUserConnectionRepository: IssueUserConnectionRepository
) {
    @SchemaMapping(typeName = "Issue", field = "accounts")
    fun accounts(issue: Issue): List<Account> {
        val id = issue.id ?: return emptyList()
        // Get all connections for this issue and extract the accounts
        return issueAccountConnectionRepository.findAccountsByIssue(id)
    }

    @SchemaMapping(typeName = "Issue", field = "commits")
    fun commits(issue: Issue): List<Commit> {
        val id = issue.id ?: return emptyList()
        // Get all connections for this issue and extract the commits
        return issueCommitConnectionRepository.findCommitsByIssue(id)
    }

    @SchemaMapping(typeName = "Issue", field = "milestones")
    fun milestones(issue: Issue): List<Milestone> {
        val id = issue.id ?: return emptyList()
        // Get all connections for this issue and extract the milestones
        return issueMilestoneConnectionRepository.findMilestonesByIssue(id)
    }

    @SchemaMapping(typeName = "Issue", field = "notes")
    fun notes(issue: Issue): List<Note> {
        val id = issue.id ?: return emptyList()
        // Get all connections for this issue and extract the notes
        return issueNoteConnectionRepository.findNotesByIssue(id)
    }

    @SchemaMapping(typeName = "Issue", field = "users")
    fun users(issue: Issue): List<User> {
        val id = issue.id ?: return emptyList()
        // Get all connections for this issue and extract the users
        return issueUserConnectionRepository.findUsersByIssue(id)
    }
}
