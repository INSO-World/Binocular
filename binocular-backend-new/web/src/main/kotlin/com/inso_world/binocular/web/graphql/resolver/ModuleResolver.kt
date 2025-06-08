package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.service.ModuleService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class ModuleResolver(
    private val moduleService: ModuleService
) {
    private val logger: Logger = LoggerFactory.getLogger(ModuleResolver::class.java)
    @SchemaMapping(typeName = "Module", field = "commits")
    fun commits(module: Module): List<Commit> {
        val id = module.id ?: return emptyList()
        logger.info("Resolving commits for module: $id")
        // Get all connections for this module and extract the commits
        return moduleService.findCommitsByModuleId(id)
    }

    @SchemaMapping(typeName = "Module", field = "files")
    fun files(module: Module): List<File> {
        val id = module.id ?: return emptyList()
        logger.info("Resolving files for module: $id")
        // Get all connections for this module and extract the files
        return moduleService.findFilesByModuleId(id)
    }

    @SchemaMapping(typeName = "Module", field = "childModules")
    fun childModules(module: Module): List<Module> {
        val id = module.id ?: return emptyList()
        logger.info("Resolving child modules for module: $id")
        // Get all connections for this module and extract the child modules
        return moduleService.findChildModulesByModuleId(id)
    }

    @SchemaMapping(typeName = "Module", field = "parentModules")
    fun parentModules(module: Module): List<Module> {
        val id = module.id ?: return emptyList()
        logger.info("Resolving parent modules for module: $id")
        // Get all connections for this module and extract the parent modules
        return moduleService.findParentModulesByModuleId(id)
    }
}
