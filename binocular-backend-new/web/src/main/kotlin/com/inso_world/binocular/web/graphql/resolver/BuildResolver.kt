package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.service.BuildService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class BuildResolver(
    private val buildService: BuildService
) {
    private val logger: Logger = LoggerFactory.getLogger(BuildResolver::class.java)

    /**
     * Resolves the commits field for a Build in GraphQL.
     * 
     * This method retrieves all commits associated with the given build.
     * If the build ID is null, an empty list is returned.
     * 
     * @param build The build for which to retrieve commits
     * @return A list of commits associated with the build, or an empty list if the build ID is null
     */
    @SchemaMapping(typeName = "Build", field = "commits")
    fun commits(build: Build): List<Commit> {
        val id = build.id ?: return emptyList()
        logger.info("Resolving commits for build: $id")
        // Get all connections for this build and extract the commits
        return buildService.findCommitsByBuildId(id)
    }

    /**
     * Resolves the commit field for a Build in GraphQL.
     * 
     * This method retrieves the first commit associated with the given build.
     * If the build ID is null or there are no commits associated with the build, null is returned.
     * 
     * @param build The build for which to retrieve the commit
     * @return The first commit associated with the build, or null if there are no commits
     */
    @SchemaMapping(typeName = "Build", field = "commit")
    fun commit(build: Build): Commit? {
        val id = build.id ?: return null
        logger.info("Resolving commit for build: $id")
        // Get all connections for this build and extract the first commit
        val commits = buildService.findCommitsByBuildId(id)
        return commits.firstOrNull()
    }
}
