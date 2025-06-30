package com.inso_world.binocular.web.persistence.dao.interfaces

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.edge.domain.MergeRequestAccountConnection

/**
 * Interface for MergeRequestAccountConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
interface IMergeRequestAccountConnectionDao {
    
    /**
     * Find all accounts connected to a merge request
     */
    fun findAccountsByMergeRequest(mergeRequestId: String): List<Account>
    
    /**
     * Find all merge requests connected to an account
     */
    fun findMergeRequestsByAccount(accountId: String): List<MergeRequest>
    
    /**
     * Save a merge request-account connection
     */
    fun save(connection: MergeRequestAccountConnection): MergeRequestAccountConnection
    
    /**
     * Delete all merge request-account connections
     */
    fun deleteAll()
}
