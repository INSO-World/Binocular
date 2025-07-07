package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge

import com.inso_world.binocular.infrastructure.arangodb.model.edge.CommitFileUserConnection
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.User

/**
 * Interface for CommitFileUserConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
internal interface ICommitFileUserConnectionDao {
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
