package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.service.AccountService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class AccountResolver(
    private val accountService: AccountService
) {
    private val logger: Logger = LoggerFactory.getLogger(AccountResolver::class.java)
    @SchemaMapping(typeName = "Account", field = "issues")
    fun issues(account: Account): List<Issue> {
        val id = account.id ?: return emptyList()
        logger.info("Resolving issues for account: $id")
        // Get all connections for this account and extract the issues
        return accountService.findIssuesByAccountId(id)
    }

    @SchemaMapping(typeName = "Account", field = "mergeRequests")
    fun mergeRequests(account: Account): List<MergeRequest> {
        val id = account.id ?: return emptyList()
        logger.info("Resolving mergeRequests for account: $id")
        // Get all connections for this account and extract the merge requests
        return accountService.findMergeRequestsByAccountId(id)
    }

    @SchemaMapping(typeName = "Account", field = "notes")
    fun notes(account: Account): List<Note> {
        val id = account.id ?: return emptyList()
        logger.info("Resolving notes for account: $id")
        // Get all connections for this account and extract the notes
        return accountService.findNotesByAccountId(id)
    }
}
