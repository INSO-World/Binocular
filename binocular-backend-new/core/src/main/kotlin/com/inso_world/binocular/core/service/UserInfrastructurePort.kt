package com.inso_world.binocular.core.service

import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.User

/**
 * Interface for UserService.
 * Provides methods to retrieve users and their related entities.
 */
interface UserInfrastructurePort : BinocularInfrastructurePort<User> {
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
