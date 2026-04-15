package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge

import com.inso_world.binocular.infrastructure.arangodb.model.edge.ModuleModuleConnection

/**
 * Interface for ModuleModuleConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
internal interface IModuleModuleConnectionDao {
    /**
     * Find all child modules connected to a parent module
     */
    fun findChildModules(parentModuleId: String): List<com.inso_world.binocular.model.Module>

    /**
     * Find all parent modules connected to a child module
     */
    fun findParentModules(childModuleId: String): List<com.inso_world.binocular.model.Module>

    /**
     * Save a module-module connection
     */
    fun save(connection: ModuleModuleConnection): ModuleModuleConnection

    /**
     * Delete all module-module connections
     */
    fun deleteAll()
}
