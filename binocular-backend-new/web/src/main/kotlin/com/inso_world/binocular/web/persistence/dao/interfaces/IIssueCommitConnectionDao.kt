package com.inso_world.binocular.web.persistence.dao.interfaces

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.edge.domain.IssueCommitConnection

/**
 * Interface for IssueCommitConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
interface IIssueCommitConnectionDao {
    
    /**
     * Find all commits connected to an issue
     */
    fun findCommitsByIssue(issueId: String): List<Commit>
    
    /**
     * Find all issues connected to a commit
     */
    fun findIssuesByCommit(commitId: String): List<Issue>
    
    /**
     * Save an issue-commit connection
     */
    fun save(connection: IssueCommitConnection): IssueCommitConnection
    
    /**
     * Delete all issue-commit connections
     */
    fun deleteAll()
}
