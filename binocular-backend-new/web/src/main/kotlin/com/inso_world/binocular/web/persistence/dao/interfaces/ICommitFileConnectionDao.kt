package com.inso_world.binocular.web.persistence.dao.interfaces

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.edge.domain.CommitFileConnection

/**
 * Interface for CommitFileConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
interface ICommitFileConnectionDao {
    
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
