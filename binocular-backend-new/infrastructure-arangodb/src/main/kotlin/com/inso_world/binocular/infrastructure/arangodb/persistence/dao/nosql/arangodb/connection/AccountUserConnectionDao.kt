package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.connection

import com.inso_world.binocular.infrastructure.arangodb.model.edge.AccountUserConnection
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IAccountUserConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.AccountUserConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.AccountMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.UserMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.AccountRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.UserRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges.AccountUserConnectionRepository
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IAccountUserConnectionDao.
 */
@Repository
class AccountUserConnectionDao
    @Autowired
    constructor(
        private val repository: AccountUserConnectionRepository,
        private val accountRepository: AccountRepository,
        private val userRepository: UserRepository,
        private val accountMapper: AccountMapper,
        private val userMapper: UserMapper,
    ) : IAccountUserConnectionDao {
        /**
         * Find all users connected to an account
         */
        override fun findUsersByAccount(accountId: String): List<User> {
            val userEntities = repository.findUsersByAccount(accountId)
            return userEntities.map { userMapper.toDomain(it) }
        }

        /**
         * Find all accounts connected to a user
         */
        override fun findAccountsByUser(userId: String): List<Account> {
            val accountEntities = repository.findAccountsByUser(userId)
            return accountEntities.map { accountMapper.toDomain(it) }
        }

        /**
         * Save an account-user connection
         */
        override fun save(connection: AccountUserConnection): AccountUserConnection {
            val accountEntity =
                accountRepository.findById(connection.from.id!!).orElseThrow {
                    IllegalArgumentException("Account with ID ${connection.from.id} not found")
                }
            val userEntity =
                userRepository.findById(connection.to.id!!).orElseThrow {
                    IllegalArgumentException("User with ID ${connection.to.id} not found")
                }

            val entity =
                AccountUserConnectionEntity(
                    id = connection.id,
                    from = accountEntity,
                    to = userEntity,
                )

            val saved = repository.save(entity)

            return AccountUserConnection(
                id = saved.id,
                from = accountMapper.toDomain(saved.from),
                to = userMapper.toDomain(saved.to),
            )
        }

        /**
         * Delete all account-user connections
         */
        override fun deleteAll() {
            repository.deleteAll()
        }
    }
