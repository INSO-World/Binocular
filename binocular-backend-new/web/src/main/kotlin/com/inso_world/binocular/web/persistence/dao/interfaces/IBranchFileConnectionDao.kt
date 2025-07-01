package com.inso_world.binocular.web.persistence.dao.interfaces

import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.edge.domain.BranchFileConnection

/**
 * Interface for BranchFileConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
interface IBranchFileConnectionDao {
    
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
