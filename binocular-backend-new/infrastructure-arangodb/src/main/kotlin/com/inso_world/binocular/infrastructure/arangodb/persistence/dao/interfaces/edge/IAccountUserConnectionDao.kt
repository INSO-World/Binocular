package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge

import com.inso_world.binocular.infrastructure.arangodb.model.edge.AccountUserConnection
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.User

/**
 * Interface for AccountUserConnection DAO operations.
 * This interface is implemented by ArangoDB and potentially other persistence layers.
 */
internal interface IAccountUserConnectionDao {
    /**
     * Find all users connected to an account
     */
    fun findUsersByAccount(accountId: String): List<User>

    /**
     * Find all accounts connected to a user
     */
    fun findAccountsByUser(userId: String): List<Account>

    /**
     * Save an account-user connection
     */
    fun save(connection: AccountUserConnection): AccountUserConnection

    /**
     * Delete all account-user connections
     */
    fun deleteAll()
}
