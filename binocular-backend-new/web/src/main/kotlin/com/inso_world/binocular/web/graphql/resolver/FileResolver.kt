package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.BranchFileConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitFileConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.ModuleFileConnectionRepository
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class FileResolver(
    private val branchFileConnectionRepository: BranchFileConnectionRepository,
    private val commitFileConnectionRepository: CommitFileConnectionRepository,
    private val moduleFileConnectionRepository: ModuleFileConnectionRepository
) {
    @SchemaMapping(typeName = "File", field = "branches")
    fun branches(file: File): List<Branch> {
        val id = file.id ?: return emptyList()
        // Get all connections for this file and extract the branches
        return branchFileConnectionRepository.findBranchesByFile(id)
    }

    @SchemaMapping(typeName = "File", field = "commits")
    fun commits(file: File): List<Commit> {
        val id = file.id ?: return emptyList()
        // Get all connections for this file and extract the commits
        return commitFileConnectionRepository.findCommitsByFile(id)
    }

    @SchemaMapping(typeName = "File", field = "modules")
    fun modules(file: File): List<Module> {
        val id = file.id ?: return emptyList()
        // Get all connections for this file and extract the modules
        return moduleFileConnectionRepository.findModulesByFile(id)
    }
}
