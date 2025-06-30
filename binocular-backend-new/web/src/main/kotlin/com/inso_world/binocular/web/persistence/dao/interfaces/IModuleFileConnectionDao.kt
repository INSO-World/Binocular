package com.inso_world.binocular.web.persistence.dao.interfaces

import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.entity.edge.domain.ModuleFileConnection

/**
 * Interface for ModuleFileConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
interface IModuleFileConnectionDao {
    
    /**
     * Find all files connected to a module
     */
    fun findFilesByModule(moduleId: String): List<File>
    
    /**
     * Find all modules connected to a file
     */
    fun findModulesByFile(fileId: String): List<Module>
    
    /**
     * Save a module-file connection
     */
    fun save(connection: ModuleFileConnection): ModuleFileConnection
    
    /**
     * Delete all module-file connections
     */
    fun deleteAll()
}
