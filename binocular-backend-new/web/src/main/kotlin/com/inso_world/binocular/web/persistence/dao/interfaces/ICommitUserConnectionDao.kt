package com.inso_world.binocular.web.persistence.dao.interfaces

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.entity.edge.domain.CommitUserConnection

/**
 * Interface for CommitUserConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
interface ICommitUserConnectionDao {
    
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
