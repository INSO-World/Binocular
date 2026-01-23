package com.inso_world.binocular.core.service

import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Reference
import com.inso_world.binocular.model.Repository

/**
 * Interface for BranchService.
 * Provides methods to retrieve branches and their related entities.
 *
 * @deprecated Use [RepositoryInfrastructurePort] instead. Branch is part of the Repository aggregate
 *             and should be accessed through its aggregate root.
 */
@Deprecated(
    message = "Use RepositoryInfrastructurePort instead. Branch is part of the Repository aggregate.",
    replaceWith = ReplaceWith("RepositoryInfrastructurePort"),
    level = DeprecationLevel.WARNING
)
interface BranchInfrastructurePort : BinocularInfrastructurePort<Branch, Reference.Id> {
    /**
     * Find files by branch ID.
     *
     * @param branchId The ID of the branch
     * @return List of files associated with the branch
     */
    fun findFilesByBranchId(branchId: String): List<File>

    fun findAll(repository: Repository): Iterable<Branch>
}
