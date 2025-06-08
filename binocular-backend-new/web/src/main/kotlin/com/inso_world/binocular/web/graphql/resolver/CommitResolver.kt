package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.service.CommitService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class CommitResolver(
  private val commitService: CommitService
) {
    private val logger: Logger = LoggerFactory.getLogger(CommitResolver::class.java)
    @SchemaMapping(typeName = "Commit", field = "builds")
    fun builds(commit: Commit): List<Build> {
        val id = commit.id ?: return emptyList()
        logger.info("Resolving builds for commit: $id")
        // Get all connections for this commit and extract the builds
        return commitService.findBuildsByCommitId(id)
    }

    @SchemaMapping(typeName = "Commit", field = "files")
    fun files(commit: Commit): List<File> {
        val id = commit.id ?: return emptyList()
        logger.info("Resolving files for commit: $id")
        // Get all connections for this commit and extract the files
        return commitService.findFilesByCommitId(id)
    }

    @SchemaMapping(typeName = "Commit", field = "modules")
    fun modules(commit: Commit): List<Module> {
        val id = commit.id ?: return emptyList()
        logger.info("Resolving modules for commit: $id")
        // Get all connections for this commit and extract the modules
        return commitService.findModulesByCommitId(id)
    }

    @SchemaMapping(typeName = "Commit", field = "users")
    fun users(commit: Commit): List<User> {
        val id = commit.id ?: return emptyList()
        logger.info("Resolving users for commit: $id")
        // Get all connections for this commit and extract the users
        return commitService.findUsersByCommitId(id)
    }

    @SchemaMapping(typeName = "Commit", field = "issues")
    fun issues(commit: Commit): List<Issue> {
        val id = commit.id ?: return emptyList()
        logger.info("Resolving issues for commit: $id")
        // Get all connections for this commit and extract the issues
        return commitService.findIssuesByCommitId(id)
    }

    @SchemaMapping(typeName = "Commit", field = "parents")
    fun parents(commit: Commit): List<Commit> {
        val id = commit.id ?: return emptyList()
        logger.info("Resolving parent commits for commit: $id")
        // Get all connections for this commit and extract the parent commits
        return commitService.findParentCommitsByChildCommitId(id)
    }

    @SchemaMapping(typeName = "Commit", field = "children")
    fun children(commit: Commit): List<Commit> {
        val id = commit.id ?: return emptyList()
        logger.info("Resolving child commits for commit: $id")
        // Get all connections for this commit and extract the child commits
        return commitService.findChildCommitsByParentCommitId(id)
    }
}
