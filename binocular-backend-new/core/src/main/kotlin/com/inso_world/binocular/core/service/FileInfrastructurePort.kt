package com.inso_world.binocular.core.service

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.User
import com.inso_world.binocular.model.Module
import org.springframework.data.domain.Pageable

/**
 * Interface for FileService.
 * Provides methods to retrieve files and their related entities.
 */
interface FileInfrastructurePort : BinocularInfrastructurePort<File> {
    /**
     * Find branches by file ID.
     *
     * @param fileId The ID of the file
     * @return List of branches associated with the file
     */
    fun findBranchesByFileId(fileId: String): List<Branch>

    /**
     * Find commits by file ID.
     *
     * @param fileId The ID of the file
     * @return List of commits associated with the file
     */
    fun findCommitsByFileId(fileId: String): List<Commit>

    /**
     * Find commits by file ID with pagination.
     */
    fun findCommitsByFileId(fileId: String, pageable: Pageable): Page<Commit>

    /**
     * Find a file by its path, if available.
     */
    fun findByPath(path: String): File?

    /**
     * Find modules by file ID.
     *
     * @param fileId The ID of the file
     * @return List of modules associated with the file
     */
    fun findModulesByFileId(fileId: String): List<Module>

    /**
     * Find related files by file ID.
     *
     * @param fileId The ID of the file
     * @return List of related files
     */
    fun findRelatedFilesByFileId(fileId: String): List<File>

    /**
     * Find users by file ID.
     *
     * @param fileId The ID of the file
     * @return List of users associated with the file
     */
    fun findUsersByFileId(fileId: String): List<User>
}
