package com.inso_world.binocular.web.persistence.dao.interfaces

import com.inso_world.binocular.web.entity.Build
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.edge.domain.CommitBuildConnection

/**
 * Interface for CommitBuildConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
interface ICommitBuildConnectionDao {
    
    /**
     * Find all builds connected to a commit
     */
    fun findBuildsByCommit(commitId: String): List<Build>
    
    /**
     * Find all commits connected to a build
     */
    fun findCommitsByBuild(buildId: String): List<Commit>
    
    /**
     * Save a commit-build connection
     */
    fun save(connection: CommitBuildConnection): CommitBuildConnection
    
    /**
     * Delete all commit-build connections
     */
    fun deleteAll()
}
