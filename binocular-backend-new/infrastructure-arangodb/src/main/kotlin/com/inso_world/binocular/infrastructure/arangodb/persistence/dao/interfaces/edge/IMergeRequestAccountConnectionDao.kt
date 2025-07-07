package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge

import com.inso_world.binocular.infrastructure.arangodb.model.edge.MergeRequestAccountConnection
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.MergeRequest

/**
 * Interface for MergeRequestAccountConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
internal interface IMergeRequestAccountConnectionDao {
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
