package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge

import com.inso_world.binocular.infrastructure.arangodb.model.edge.IssueCommitConnection
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Issue

/**
 * Interface for IssueCommitConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
internal interface IIssueCommitConnectionDao {
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
