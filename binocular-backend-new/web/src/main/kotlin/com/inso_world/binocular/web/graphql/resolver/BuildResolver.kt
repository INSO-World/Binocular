package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitBuildConnectionRepository
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class BuildResolver(
    private val commitBuildConnectionRepository: CommitBuildConnectionRepository
) {
    @SchemaMapping(typeName = "Build", field = "commits")
    fun commits(build: Build): List<Commit> {
        val id = build.id ?: return emptyList()
        // Get all connections for this build and extract the commits
        return commitBuildConnectionRepository.findCommitsByBuild(id)
    }
}
