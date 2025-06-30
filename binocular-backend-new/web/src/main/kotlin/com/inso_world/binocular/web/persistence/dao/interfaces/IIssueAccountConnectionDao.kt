package com.inso_world.binocular.web.persistence.dao.interfaces

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.edge.domain.IssueAccountConnection

/**
 * Interface for IssueAccountConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
interface IIssueAccountConnectionDao {
    
    /**
     * Find all accounts connected to an issue
     */
    fun findAccountsByIssue(issueId: String): List<Account>
    
    /**
     * Find all issues connected to an account
     */
    fun findIssuesByAccount(accountId: String): List<Issue>
    
    /**
     * Save an issue-account connection
     */
    fun save(connection: IssueAccountConnection): IssueAccountConnection
    
    /**
     * Delete all issue-account connections
     */
    fun deleteAll()
}
