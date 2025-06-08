package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.*
import org.springframework.data.domain.Pageable

/**
 * Interface for CommitService.
 * Provides methods to retrieve commits and their related entities.
 */
interface CommitService {
    /**
     * Find all commits with pagination.
     *
     * @param pageable Pagination information
     * @return Iterable of commits
     */
    fun findAll(pageable: Pageable): Iterable<Commit>

    /**
     * Find a commit by ID.
     *
     * @param id The ID of the commit to find
     * @return The commit if found, null otherwise
     */
    fun findById(id: String): Commit?

    /**
     * Find builds by commit ID.
     *
     * @param commitId The ID of the commit
     * @return List of builds associated with the commit
     */
    fun findBuildsByCommitId(commitId: String): List<Build>

    /**
     * Find files by commit ID.
     *
     * @param commitId The ID of the commit
     * @return List of files associated with the commit
     */
    fun findFilesByCommitId(commitId: String): List<File>

    /**
     * Find modules by commit ID.
     *
     * @param commitId The ID of the commit
     * @return List of modules associated with the commit
     */
    fun findModulesByCommitId(commitId: String): List<Module>

    /**
     * Find users by commit ID.
     *
     * @param commitId The ID of the commit
     * @return List of users associated with the commit
     */
    fun findUsersByCommitId(commitId: String): List<User>

    /**
     * Find issues by commit ID.
     *
     * @param commitId The ID of the commit
     * @return List of issues associated with the commit
     */
    fun findIssuesByCommitId(commitId: String): List<Issue>

    /**
     * Find parent commits by child commit ID.
     *
     * @param childCommitId The ID of the child commit
     * @return List of parent commits
     */
    fun findParentCommitsByChildCommitId(childCommitId: String): List<Commit>

    /**
     * Find child commits by parent commit ID.
     *
     * @param parentCommitId The ID of the parent commit
     * @return List of child commits
     */
    fun findChildCommitsByParentCommitId(parentCommitId: String): List<Commit>
}
