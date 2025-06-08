package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.User
import org.springframework.data.domain.Pageable

/**
 * Interface for UserService.
 * Provides methods to retrieve users and their related entities.
 */
interface UserService {
    /**
     * Find all users with pagination.
     *
     * @param pageable Pagination information
     * @return Iterable of users
     */
    fun findAll(pageable: Pageable): Iterable<User>

    /**
     * Find a user by ID.
     *
     * @param id The ID of the user to find
     * @return The user if found, null otherwise
     */
    fun findById(id: String): User?

    /**
     * Find commits by user ID.
     *
     * @param userId The ID of the user
     * @return List of commits associated with the user
     */
    fun findCommitsByUserId(userId: String): List<Commit>

    /**
     * Find issues by user ID.
     *
     * @param userId The ID of the user
     * @return List of issues associated with the user
     */
    fun findIssuesByUserId(userId: String): List<Issue>

    /**
     * Find files by user ID.
     *
     * @param userId The ID of the user
     * @return List of files associated with the user
     */
    fun findFilesByUserId(userId: String): List<File>
}
