package com.inso_world.binocular.web.persistence.dao.interfaces

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.edge.domain.CommitCommitConnection

/**
 * Interface for CommitCommitConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
interface ICommitCommitConnectionDao {
    
    /**
     * Find all child commits connected to a parent commit
     */
    fun findChildCommits(parentCommitId: String): List<Commit>
    
    /**
     * Find all parent commits connected to a child commit
     */
    fun findParentCommits(childCommitId: String): List<Commit>
    
    /**
     * Save a commit-commit connection
     */
    fun save(connection: CommitCommitConnection): CommitCommitConnection
    
    /**
     * Delete all commit-commit connections
     */
    fun deleteAll()
}
