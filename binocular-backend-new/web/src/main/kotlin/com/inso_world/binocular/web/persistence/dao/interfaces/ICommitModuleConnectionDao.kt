package com.inso_world.binocular.web.persistence.dao.interfaces

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.entity.edge.domain.CommitModuleConnection

/**
 * Interface for CommitModuleConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
interface ICommitModuleConnectionDao {
    
    /**
     * Find all modules connected to a commit
     */
    fun findModulesByCommit(commitId: String): List<Module>
    
    /**
     * Find all commits connected to a module
     */
    fun findCommitsByModule(moduleId: String): List<Commit>
    
    /**
     * Save a commit-module connection
     */
    fun save(connection: CommitModuleConnection): CommitModuleConnection
    
    /**
     * Delete all commit-module connections
     */
    fun deleteAll()
}
