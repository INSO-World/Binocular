package com.inso_world.binocular.web.service.interfaces

import com.inso_world.binocular.web.entity.Build
import com.inso_world.binocular.web.entity.Commit
import org.springframework.data.domain.Pageable

/**
 * Interface for BuildService.
 * Provides methods to retrieve builds and their related entities.
 */
interface BuildServiceInterface {
    /**
     * Find all builds with pagination.
     *
     * @param pageable Pagination information
     * @return Iterable of builds
     */
    fun findAll(pageable: Pageable): Iterable<Build>

    /**
     * Find a build by ID.
     *
     * @param id The ID of the build to find
     * @return The build if found, null otherwise
     */
    fun findById(id: String): Build?

    /**
     * Find commits by build ID.
     *
     * @param buildId The ID of the build
     * @return List of commits associated with the build
     */
    fun findCommitsByBuildId(buildId: String): List<Commit>
}
