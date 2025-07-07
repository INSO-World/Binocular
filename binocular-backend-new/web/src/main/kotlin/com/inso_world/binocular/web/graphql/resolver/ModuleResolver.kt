package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.core.service.ModuleInfrastructurePort
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class ModuleResolver(
    private val moduleService: ModuleInfrastructurePort,
) {
    private val logger: Logger = LoggerFactory.getLogger(ModuleResolver::class.java)

    /**
     * Resolves the commits field for a Module in GraphQL.
     *
     * This method retrieves all commits associated with the given module.
     * If the module ID is null, an empty list is returned.
     *
     * @param module The module for which to retrieve commits
     * @return A list of commits associated with the module, or an empty list if the module ID is null
     */
    @SchemaMapping(typeName = "Module", field = "commits")
    fun commits(module: com.inso_world.binocular.model.Module): List<Commit> {
        val id = module.id ?: return emptyList()
        logger.info("Resolving commits for module: $id")
        // Get all connections for this module and extract the commits
        return moduleService.findCommitsByModuleId(id)
    }

    /**
     * Resolves the files field for a Module in GraphQL.
     *
     * This method retrieves all files associated with the given module.
     * If the module ID is null, an empty list is returned.
     *
     * @param module The module for which to retrieve files
     * @return A list of files associated with the module, or an empty list if the module ID is null
     */
    @SchemaMapping(typeName = "Module", field = "files")
    fun files(module: com.inso_world.binocular.model.Module): List<File> {
        val id = module.id ?: return emptyList()
        logger.info("Resolving files for module: $id")
        // Get all connections for this module and extract the files
        return moduleService.findFilesByModuleId(id)
    }

    /**
     * Resolves the childModules field for a Module in GraphQL.
     *
     * This method retrieves all child modules associated with the given module.
     * If the module ID is null, an empty list is returned.
     *
     * @param module The module for which to retrieve child modules
     * @return A list of child modules associated with the module, or an empty list if the module ID is null
     */
    @SchemaMapping(typeName = "Module", field = "childModules")
    fun childModules(module: com.inso_world.binocular.model.Module): List<com.inso_world.binocular.model.Module> {
        val id = module.id ?: return emptyList()
        logger.info("Resolving child modules for module: $id")
        // Get all connections for this module and extract the child modules
        return moduleService.findChildModulesByModuleId(id)
    }

    /**
     * Resolves the parentModules field for a Module in GraphQL.
     *
     * This method retrieves all parent modules associated with the given module.
     * If the module ID is null, an empty list is returned.
     *
     * @param module The module for which to retrieve parent modules
     * @return A list of parent modules associated with the module, or an empty list if the module ID is null
     */
    @SchemaMapping(typeName = "Module", field = "parentModules")
    fun parentModules(module: com.inso_world.binocular.model.Module): List<com.inso_world.binocular.model.Module> {
        val id = module.id ?: return emptyList()
        logger.info("Resolving parent modules for module: $id")
        // Get all connections for this module and extract the parent modules
        return moduleService.findParentModulesByModuleId(id)
    }
}
