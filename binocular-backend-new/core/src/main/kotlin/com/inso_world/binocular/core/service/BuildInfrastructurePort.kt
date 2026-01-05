package com.inso_world.binocular.core.service

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.model.Build
import com.inso_world.binocular.model.Commit
import org.springframework.data.domain.Pageable

/**
 * Interface for BuildService.
 * Provides methods to retrieve builds and their related entities.
 */
interface BuildInfrastructurePort : BinocularInfrastructurePort<Build> {
    /**
     * Find all builds with pagination and timestamp filter.
     *
     * @param pageable Pagination information
     * @param until Timestamp to filter builds (only include builds before this timestamp)
     * @return Page of builds
     */
    fun findAll(
        pageable: Pageable,
        since: Long?,
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
