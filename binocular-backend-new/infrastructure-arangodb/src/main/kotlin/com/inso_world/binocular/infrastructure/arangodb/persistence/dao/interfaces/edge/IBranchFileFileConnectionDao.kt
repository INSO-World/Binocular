package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces

import com.inso_world.binocular.model.File

/**
 * Interface for BranchFileFileConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
internal interface IBranchFileFileConnectionDao {
    /**
     * Find all files connected to a branch file
     */
    fun findFilesByBranchFile(branchFileId: String): List<File>

    /**
     * Find all branch files connected to a file
     */
    fun findBranchFilesByFile(fileId: String): List<File>

    /**
     * Delete all branch-file-file connections
     */
    fun deleteAll()
}
