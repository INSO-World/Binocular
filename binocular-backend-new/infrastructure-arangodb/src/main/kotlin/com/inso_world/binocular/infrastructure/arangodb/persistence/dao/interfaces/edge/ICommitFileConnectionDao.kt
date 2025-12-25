package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge

import com.inso_world.binocular.infrastructure.arangodb.model.edge.CommitFileConnection
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Stats

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
     * Return stats (additions/deletions) for a given commit
     */
    fun findCommitStatsByCommit(commitId: String): Stats

    /**
     * Return stats per file for a given commit, keyed by file id
     */
    fun findFileStatsByCommit(commitId: String): Map<String, Stats>

    /**
     * Save a commit-file connection
     */
    fun save(connection: CommitFileConnection): CommitFileConnection

    /**
     * Delete all commit-file connections
     */
    fun deleteAll()
}
