package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.entity.User
import org.springframework.data.domain.Pageable

/**
 * Interface for FileService.
 * Provides methods to retrieve files and their related entities.
 */
interface FileService {
    /**
     * Find all files with pagination.
     *
     * @param pageable Pagination information
     * @return Iterable of files
     */
    fun findAll(pageable: Pageable): Iterable<File>

    /**
     * Find a file by ID.
     *
     * @param id The ID of the file to find
     * @return The file if found, null otherwise
     */
    fun findById(id: String): File?

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
