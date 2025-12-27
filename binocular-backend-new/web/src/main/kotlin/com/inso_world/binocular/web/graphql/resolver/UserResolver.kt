package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.core.service.AccountInfrastructurePort
import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class UserResolver(
    private val userService: UserInfrastructurePort,
    private val accountService: AccountInfrastructurePort,
) {
    private val logger: Logger = LoggerFactory.getLogger(UserResolver::class.java)

    /**
     * Resolves the commits field for a User in GraphQL.
     *
     * This method retrieves all commits associated with the given user.
     * If the user ID is null, an empty list is returned.
     *
     * @param user The user for which to retrieve commits
     * @return A list of commits associated with the user, or an empty list if the user ID is null
     */
    @SchemaMapping(typeName = "User", field = "commits")
    fun commits(user: User): List<Commit> {
        val id = user.id ?: return emptyList()
        logger.info("Resolving commits for user: $id")
        // Get all connections for this user and extract the commits
        return userService.findCommitsByUserId(id)
    }

    /**
     * Resolves the issues field for a User in GraphQL.
     *
     * This method retrieves all issues associated with the given user.
     * If the user ID is null, an empty list is returned.
     *
     * @param user The user for which to retrieve issues
     * @return A list of issues associated with the user, or an empty list if the user ID is null
     */
    @SchemaMapping(typeName = "User", field = "issues")
    fun issues(user: User): List<Issue> {
        val id = user.id ?: return emptyList()
        logger.info("Resolving issues for user: $id")
        // Get all connections for this user and extract the issues
        return userService.findIssuesByUserId(id)
    }

    /**
     * Resolves the files field for a User in GraphQL.
     *
     * This method retrieves all files associated with the given user.
     * If the user ID is null, an empty list is returned.
     *
     * @param user The user for which to retrieve files
     * @return A list of files associated with the user, or an empty list if the user ID is null
     */
    @SchemaMapping(typeName = "User", field = "files")
    fun files(user: User): List<File> {
        val id = user.id ?: return emptyList()
        logger.info("Resolving files for user: $id")
        // Get all connections for this user and extract the files
        return userService.findFilesByUserId(id)
    }

    @SchemaMapping(typeName = "User", field = "account")
    fun account(user: User): Account? {
        val userId = user.id ?: return null
        logger.info("Resolving account for user: ${'$'}userId")
        val accounts = accountService.findAccountsByUserId(userId)
        return accounts.firstOrNull()
    }

}
