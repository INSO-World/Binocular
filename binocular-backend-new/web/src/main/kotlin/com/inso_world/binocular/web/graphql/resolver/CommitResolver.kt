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
    /**
     * Resolves the builds field for a Commit in GraphQL.
     * 
     * This method retrieves all builds associated with the given commit.
     * If the commit ID is null, an empty list is returned.
     * 
     * @param commit The commit for which to retrieve builds
     * @return A list of builds associated with the commit, or an empty list if the commit ID is null
     */
    @SchemaMapping(typeName = "Commit", field = "builds")
    fun builds(commit: Commit): List<Build> {
        val id = commit.id ?: return emptyList()
        logger.info("Resolving builds for commit: $id")
        // Get all connections for this commit and extract the builds
        return commitService.findBuildsByCommitId(id)
    }

    /**
     * Resolves the files field for a Commit in GraphQL.
     * 
     * This method retrieves all files associated with the given commit.
     * If the commit ID is null, an empty list is returned.
     * 
     * @param commit The commit for which to retrieve files
     * @return A list of files associated with the commit, or an empty list if the commit ID is null
     */
    @SchemaMapping(typeName = "Commit", field = "files")
    fun files(commit: Commit): List<File> {
        val id = commit.id ?: return emptyList()
        logger.info("Resolving files for commit: $id")
        // Get all connections for this commit and extract the files
        return commitService.findFilesByCommitId(id)
    }

    /**
     * Resolves the modules field for a Commit in GraphQL.
     * 
     * This method retrieves all modules associated with the given commit.
     * If the commit ID is null, an empty list is returned.
     * 
     * @param commit The commit for which to retrieve modules
     * @return A list of modules associated with the commit, or an empty list if the commit ID is null
     */
    @SchemaMapping(typeName = "Commit", field = "modules")
    fun modules(commit: Commit): List<Module> {
        val id = commit.id ?: return emptyList()
        logger.info("Resolving modules for commit: $id")
        // Get all connections for this commit and extract the modules
        return commitService.findModulesByCommitId(id)
    }

    /**
     * Resolves the users field for a Commit in GraphQL.
     * 
     * This method retrieves all users associated with the given commit.
     * If the commit ID is null, an empty list is returned.
     * 
     * @param commit The commit for which to retrieve users
     * @return A list of users associated with the commit, or an empty list if the commit ID is null
     */
    @SchemaMapping(typeName = "Commit", field = "users")
    fun users(commit: Commit): List<User> {
        val id = commit.id ?: return emptyList()
        logger.info("Resolving users for commit: $id")
        // Get all connections for this commit and extract the users
        return commitService.findUsersByCommitId(id)
    }

    /**
     * Resolves the issues field for a Commit in GraphQL.
     * 
     * This method retrieves all issues associated with the given commit.
     * If the commit ID is null, an empty list is returned.
     * 
     * @param commit The commit for which to retrieve issues
     * @return A list of issues associated with the commit, or an empty list if the commit ID is null
     */
    @SchemaMapping(typeName = "Commit", field = "issues")
    fun issues(commit: Commit): List<Issue> {
        val id = commit.id ?: return emptyList()
        logger.info("Resolving issues for commit: $id")
        // Get all connections for this commit and extract the issues
        return commitService.findIssuesByCommitId(id)
    }

    /**
     * Resolves the parents field for a Commit in GraphQL.
     * 
     * This method retrieves all parent commits associated with the given commit.
     * If the commit ID is null, an empty list is returned.
     * 
     * @param commit The commit for which to retrieve parent commits
     * @return A list of parent commits associated with the commit, or an empty list if the commit ID is null
     */
    @SchemaMapping(typeName = "Commit", field = "parents")
    fun parents(commit: Commit): List<Commit> {
        val id = commit.id ?: return emptyList()
        logger.info("Resolving parent commits for commit: $id")
        // Get all connections for this commit and extract the parent commits
        return commitService.findParentCommitsByChildCommitId(id)
    }

    /**
     * Resolves the children field for a Commit in GraphQL.
     * 
     * This method retrieves all child commits associated with the given commit.
     * If the commit ID is null, an empty list is returned.
     * 
     * @param commit The commit for which to retrieve child commits
     * @return A list of child commits associated with the commit, or an empty list if the commit ID is null
     */
    @SchemaMapping(typeName = "Commit", field = "children")
    fun children(commit: Commit): List<Commit> {
        val id = commit.id ?: return emptyList()
        logger.info("Resolving child commits for commit: $id")
        // Get all connections for this commit and extract the child commits
        return commitService.findChildCommitsByParentCommitId(id)
    }
}
