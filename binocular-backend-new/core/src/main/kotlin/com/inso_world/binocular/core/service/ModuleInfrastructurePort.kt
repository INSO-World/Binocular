package com.inso_world.binocular.core.service

import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File

/**
 * Interface for ModuleService.
 * Provides methods to retrieve modules and their related entities.
 *
 * @deprecated Use [RepositoryInfrastructurePort] instead. Module is part of the Repository aggregate
 *             and should be accessed through its aggregate root.
 */
@Deprecated(
    message = "Use RepositoryInfrastructurePort instead. Module is part of the Repository aggregate.",
    replaceWith = ReplaceWith("RepositoryInfrastructurePort"),
    level = DeprecationLevel.WARNING
)
interface ModuleInfrastructurePort : BinocularInfrastructurePort<com.inso_world.binocular.model.Module, com.inso_world.binocular.model.Module.Id> {
    /**
     * Find commits by module ID.
     *
     * @param moduleId The ID of the module
     * @return List of commits associated with the module
     */
    fun findCommitsByModuleId(moduleId: String): List<Commit>

    /**
     * Find files by module ID.
     *
     * @param moduleId The ID of the module
     * @return List of files associated with the module
     */
    fun findFilesByModuleId(moduleId: String): List<File>

    /**
     * Find child modules by module ID.
     *
     * @param moduleId The ID of the module
     * @return List of child modules
     */
    fun findChildModulesByModuleId(moduleId: String): List<com.inso_world.binocular.model.Module>

    /**
     * Find parent modules by module ID.
     *
     * @param moduleId The ID of the module
     * @return List of parent modules
     */
    fun findParentModulesByModuleId(moduleId: String): List<com.inso_world.binocular.model.Module>
}
