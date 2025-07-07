package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge

import com.inso_world.binocular.infrastructure.arangodb.model.edge.CommitModuleConnection
import com.inso_world.binocular.model.Commit

/**
 * Interface for CommitModuleConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
internal interface ICommitModuleConnectionDao {
    /**
     * Find all modules connected to a commit
     */
    fun findModulesByCommit(commitId: String): List<com.inso_world.binocular.model.Module>

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
