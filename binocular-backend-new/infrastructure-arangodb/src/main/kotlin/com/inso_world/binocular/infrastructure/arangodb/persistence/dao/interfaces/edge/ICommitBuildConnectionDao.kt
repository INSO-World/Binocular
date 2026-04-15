package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces

import com.inso_world.binocular.infrastructure.arangodb.model.edge.CommitBuildConnection
import com.inso_world.binocular.model.Build
import com.inso_world.binocular.model.Commit

/**
 * Interface for CommitBuildConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
internal interface ICommitBuildConnectionDao {
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
