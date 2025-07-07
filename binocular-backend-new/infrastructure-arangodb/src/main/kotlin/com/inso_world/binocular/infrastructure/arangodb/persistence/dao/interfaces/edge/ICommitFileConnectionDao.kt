package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge

import com.inso_world.binocular.infrastructure.arangodb.model.edge.CommitFileConnection
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File

/**
 * Interface for CommitFileConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
internal interface ICommitFileConnectionDao {
    /**
     * Find all files connected to a commit
     */
    fun findFilesByCommit(commitId: String): List<File>

    /**
     * Find all commits connected to a file
     */
    fun findCommitsByFile(fileId: String): List<Commit>

    /**
     * Save a commit-file connection
     */
    fun save(connection: CommitFileConnection): CommitFileConnection

    /**
     * Delete all commit-file connections
     */
    fun deleteAll()
}
