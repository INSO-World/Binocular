package com.inso_world.binocular.core.service

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.model.Build
import com.inso_world.binocular.model.Commit
import org.springframework.data.domain.Pageable

/**
 * Interface for BuildService.
 * Provides methods to retrieve builds and their related entities.
 *
 * @deprecated Use [RepositoryInfrastructurePort] instead. Build is part of the Repository aggregate
 *             and should be accessed through its aggregate root.
 */
@Deprecated(
    message = "Use RepositoryInfrastructurePort instead. Build is part of the Repository aggregate.",
    replaceWith = ReplaceWith("RepositoryInfrastructurePort"),
    level = DeprecationLevel.WARNING
)
interface BuildInfrastructurePort : BinocularInfrastructurePort<Build, Build.Id> {
    /**
     * Find all builds with pagination and timestamp filter.
     *
     * @param pageable Pagination information
     * @param until Timestamp to filter builds (only include builds before this timestamp)
     * @return Page of builds
     */
    fun findAll(
        pageable: Pageable,
        until: Long?,
    ): Page<Build>

    /**
     * Find commits by build ID.
     *
     * @param buildId The ID of the build
     * @return List of commits associated with the build
     */
    fun findCommitsByBuildId(buildId: String): List<Commit>
}
