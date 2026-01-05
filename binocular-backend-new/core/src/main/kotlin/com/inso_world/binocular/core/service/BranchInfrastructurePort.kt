package com.inso_world.binocular.core.service

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Repository
import org.springframework.data.domain.Pageable

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
    fun findFilesByBranchId(branchId: String): List<File>

    /**
     * Find files by branch ID with pagination.
     */
    fun findFilesByBranchId(branchId: String, pageable: Pageable): Page<File>

    fun findAll(repository: Repository): Iterable<Branch>

    /**
     * Find a branch by its name.
     * Implementations should query the database rather than scanning in memory.
     */
    fun findByName(name: String): Branch?
}
