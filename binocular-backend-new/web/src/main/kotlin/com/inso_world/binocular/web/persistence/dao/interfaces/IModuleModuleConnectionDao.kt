package com.inso_world.binocular.web.persistence.dao.interfaces

import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.entity.edge.domain.ModuleModuleConnection

/**
 * Interface for ModuleModuleConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
interface IModuleModuleConnectionDao {
    
    /**
     * Find all child modules connected to a parent module
     */
    fun findChildModules(parentModuleId: String): List<Module>
    
    /**
     * Find all parent modules connected to a child module
     */
    fun findParentModules(childModuleId: String): List<Module>
    
    /**
     * Save a module-module connection
     */
    fun save(connection: ModuleModuleConnection): ModuleModuleConnection
    
    /**
     * Delete all module-module connections
     */
    fun deleteAll()
}
