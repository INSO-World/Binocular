package com.inso_world.binocular.web.persistence.dao.interfaces

import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.entity.edge.domain.IssueUserConnection

/**
 * Interface for IssueUserConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
interface IIssueUserConnectionDao {
    
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
