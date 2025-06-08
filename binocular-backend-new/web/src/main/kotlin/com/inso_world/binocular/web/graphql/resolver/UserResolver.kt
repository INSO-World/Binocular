package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class UserResolver(
    private val userService: UserService
) {
    private val logger: Logger = LoggerFactory.getLogger(UserResolver::class.java)
    @SchemaMapping(typeName = "User", field = "commits")
    fun commits(user: User): List<Commit> {
        val id = user.id ?: return emptyList()
        logger.info("Resolving commits for user: $id")
        // Get all connections for this user and extract the commits
        return userService.findCommitsByUserId(id)
    }

    @SchemaMapping(typeName = "User", field = "issues")
    fun issues(user: User): List<Issue> {
        val id = user.id ?: return emptyList()
        logger.info("Resolving issues for user: $id")
        // Get all connections for this user and extract the issues
        return userService.findIssuesByUserId(id)
    }

    @SchemaMapping(typeName = "User", field = "files")
    fun files(user: User): List<File> {
        val id = user.id ?: return emptyList()
        logger.info("Resolving files for user: $id")
        // Get all connections for this user and extract the files
        return userService.findFilesByUserId(id)
    }
}
