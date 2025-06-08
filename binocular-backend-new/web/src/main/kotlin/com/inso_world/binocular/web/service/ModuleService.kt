package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.Module
import org.springframework.data.domain.Pageable

/**
 * Interface for ModuleService.
 * Provides methods to retrieve modules and their related entities.
 */
interface ModuleService {
    /**
     * Find all modules with pagination.
     *
     * @param pageable Pagination information
     * @return Iterable of modules
     */
    fun findAll(pageable: Pageable): Iterable<Module>

    /**
     * Find a module by ID.
     *
     * @param id The ID of the module to find
     * @return The module if found, null otherwise
     */
    fun findById(id: String): Module?

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
    fun findChildModulesByModuleId(moduleId: String): List<Module>

    /**
     * Find parent modules by module ID.
     *
     * @param moduleId The ID of the module
     * @return List of parent modules
     */
    fun findParentModulesByModuleId(moduleId: String): List<Module>
}
