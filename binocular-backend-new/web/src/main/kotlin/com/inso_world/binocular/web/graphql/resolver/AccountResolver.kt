package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.core.service.AccountInfrastructurePort
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Note
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class AccountResolver(
    private val accountService: AccountInfrastructurePort,
) {
    private val logger: Logger = LoggerFactory.getLogger(AccountResolver::class.java)

    /**
     * Resolves the issues field for an Account in GraphQL.
     *
     * This method retrieves all issues associated with the given account.
     * If the account ID is null, an empty list is returned.
     *
     * @param account The account for which to retrieve issues
     * @return A list of issues associated with the account, or an empty list if the account ID is null
     */
    @SchemaMapping(typeName = "Account", field = "issues")
    fun issues(account: Account): List<Issue> {
        val id = account.id ?: return emptyList()
        logger.info("Resolving issues for account: $id")
        // Get all connections for this account and extract the issues
        return accountService.findIssuesByAccountId(id)
    }

    /**
     * Resolves the mergeRequests field for an Account in GraphQL.
     *
     * This method retrieves all merge requests associated with the given account.
     * If the account ID is null, an empty list is returned.
     *
     * @param account The account for which to retrieve merge requests
     * @return A list of merge requests associated with the account, or an empty list if the account ID is null
     */
    @SchemaMapping(typeName = "Account", field = "mergeRequests")
    fun mergeRequests(account: Account): List<MergeRequest> {
        val id = account.id ?: return emptyList()
        logger.info("Resolving mergeRequests for account: $id")
        // Get all connections for this account and extract the merge requests
        return accountService.findMergeRequestsByAccountId(id)
    }

    /**
     * Resolves the notes field for an Account in GraphQL.
     *
     * This method retrieves all notes associated with the given account.
     * If the account ID is null, an empty list is returned.
     *
     * @param account The account for which to retrieve notes
     * @return A list of notes associated with the account, or an empty list if the account ID is null
     */
    @SchemaMapping(typeName = "Account", field = "notes")
    fun notes(account: Account): List<Note> {
        val id = account.id ?: return emptyList()
        logger.info("Resolving notes for account: $id")
        // Get all connections for this account and extract the notes
        return accountService.findNotesByAccountId(id)
    }
}
