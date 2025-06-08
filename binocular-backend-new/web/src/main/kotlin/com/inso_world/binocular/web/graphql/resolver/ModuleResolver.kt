package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitModuleConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.ModuleFileConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.ModuleModuleConnectionRepository
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class ModuleResolver(
    private val commitModuleConnectionRepository: CommitModuleConnectionRepository,
    private val moduleFileConnectionRepository: ModuleFileConnectionRepository,
    private val moduleModuleConnectionRepository: ModuleModuleConnectionRepository
) {
    @SchemaMapping(typeName = "Module", field = "commits")
    fun commits(module: Module): List<Commit> {
        val id = module.id ?: return emptyList()
        // Get all connections for this module and extract the commits
        return commitModuleConnectionRepository.findCommitsByModule(id)
    }

    @SchemaMapping(typeName = "Module", field = "files")
    fun files(module: Module): List<File> {
        val id = module.id ?: return emptyList()
        // Get all connections for this module and extract the files
        return moduleFileConnectionRepository.findFilesByModule(id)
    }

    @SchemaMapping(typeName = "Module", field = "childModules")
    fun childModules(module: Module): List<Module> {
        val id = module.id ?: return emptyList()
        // Get all connections for this module and extract the child modules
        return moduleModuleConnectionRepository.findChildModulesByModule(id)
    }

    @SchemaMapping(typeName = "Module", field = "parentModules")
    fun parentModules(module: Module): List<Module> {
        val id = module.id ?: return emptyList()
        // Get all connections for this module and extract the parent modules
        return moduleModuleConnectionRepository.findParentModulesByModule(id)
    }
}
