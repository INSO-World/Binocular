package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge

import com.inso_world.binocular.infrastructure.arangodb.model.edge.IssueUserConnection
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.User

/**
 * Interface for IssueUserConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
internal interface IIssueUserConnectionDao {
    /**
     * Find all users connected to an issue
     */
    fun findUsersByIssue(issueId: String): List<User>

    /**
     * Find all issues connected to a user
     */
    fun findIssuesByUser(userId: String): List<Issue>

    /**
     * Save an issue-user connection
     */
    fun save(connection: IssueUserConnection): IssueUserConnection

    /**
     * Delete all issue-user connections
     */
    fun deleteAll()
}
