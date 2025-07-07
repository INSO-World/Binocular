package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge

import com.inso_world.binocular.infrastructure.arangodb.model.edge.CommitUserConnection
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.User

/**
 * Interface for CommitUserConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
internal interface ICommitUserConnectionDao {
    /**
     * Find all users connected to a commit
     */
    fun findUsersByCommit(commitId: String): List<User>

    /**
     * Find all commits connected to a user
     */
    fun findCommitsByUser(userId: String): List<Commit>

    /**
     * Save a commit-user connection
     */
    fun save(connection: CommitUserConnection): CommitUserConnection

    /**
     * Delete all commit-user connections
     */
    fun deleteAll()
}
