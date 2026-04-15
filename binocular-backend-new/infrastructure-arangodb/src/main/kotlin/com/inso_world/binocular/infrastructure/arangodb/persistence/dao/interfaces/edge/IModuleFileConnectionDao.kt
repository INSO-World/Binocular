package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge

import com.inso_world.binocular.infrastructure.arangodb.model.edge.ModuleFileConnection
import com.inso_world.binocular.model.File

/**
 * Interface for ModuleFileConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
internal interface IModuleFileConnectionDao {
    /**
     * Find all files connected to a module
     */
    fun findFilesByModule(moduleId: String): List<File>

    /**
     * Find all modules connected to a file
     */
    fun findModulesByFile(fileId: String): List<com.inso_world.binocular.model.Module>

    /**
     * Save a module-file connection
     */
    fun save(connection: ModuleFileConnection): ModuleFileConnection

    /**
     * Delete all module-file connections
     */
    fun deleteAll()
}
