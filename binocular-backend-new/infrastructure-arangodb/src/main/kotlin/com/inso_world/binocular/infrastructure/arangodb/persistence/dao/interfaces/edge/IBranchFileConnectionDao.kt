package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces

import com.inso_world.binocular.infrastructure.arangodb.model.edge.BranchFileConnection
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.File

/**
 * Interface for BranchFileConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
internal interface IBranchFileConnectionDao {
    /**
     * Find all files connected to a branch
     */
    fun findFilesByBranch(branchId: String): List<File>

    /**
     * Find all branches connected to a file
     */
    fun findBranchesByFile(fileId: String): List<Branch>

    /**
     * Save a branch-file connection
     */
    fun save(connection: BranchFileConnection): BranchFileConnection

    /**
     * Delete all branch-file connections
     */
    fun deleteAll()
}
