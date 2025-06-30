package com.inso_world.binocular.web.persistence.dao.interfaces

import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.entity.edge.domain.CommitFileUserConnection

/**
 * Interface for CommitFileUserConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
interface ICommitFileUserConnectionDao {

    /**
     * Find all users connected to a file
     */
    fun findUsersByFile(fileId: String): List<User>

    /**
     * Find all files connected to a user
     */
    fun findFilesByUser(userId: String): List<File>

    /**
     * Save a commit-file-user connection
     */
    fun save(connection: CommitFileUserConnection): CommitFileUserConnection

    /**
     * Delete all commit-file-user connections
     */
    fun deleteAll()
}
