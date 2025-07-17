package com.inso_world.binocular.core.service

import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.File
import org.springframework.transaction.annotation.Transactional

/**
 * Interface for BranchService.
 * Provides methods to retrieve branches and their related entities.
 */
interface BranchInfrastructurePort : BinocularInfrastructurePort<Branch> {
    /**
     * Find files by branch ID.
     *
     * @param branchId The ID of the branch
     * @return List of files associated with the branch
     */
    @Transactional(readOnly = true)
    fun findFilesByBranchId(branchId: String): List<File>
}
