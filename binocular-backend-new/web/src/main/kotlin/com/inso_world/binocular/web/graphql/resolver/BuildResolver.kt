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
    @SchemaMapping(typeName = "Build", field = "commits")
    fun commits(build: Build): List<Commit> {
        val id = build.id ?: return emptyList()
        logger.info("Resolving commits for build: $id")
        // Get all connections for this build and extract the commits
        return buildService.findCommitsByBuildId(id)
    }
}
