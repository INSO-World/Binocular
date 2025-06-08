package com.inso_world.binocular.web.service.interfaces

import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.entity.File
import org.springframework.data.domain.Pageable

/**
 * Interface for BranchService.
 * Provides methods to retrieve branches and their related entities.
 */
interface BranchServiceInterface {
    /**
     * Find all branches with pagination.
     *
     * @param pageable Pagination information
     * @return Iterable of branches
     */
    fun findAll(pageable: Pageable): Iterable<Branch>

    /**
     * Find a branch by ID.
     *
     * @param id The ID of the branch to find
     * @return The branch if found, null otherwise
     */
    fun findById(id: String): Branch?

    /**
     * Find files by branch ID.
     *
     * @param branchId The ID of the branch
     * @return List of files associated with the branch
     */
    fun findFilesByBranchId(branchId: String): List<File>
}
