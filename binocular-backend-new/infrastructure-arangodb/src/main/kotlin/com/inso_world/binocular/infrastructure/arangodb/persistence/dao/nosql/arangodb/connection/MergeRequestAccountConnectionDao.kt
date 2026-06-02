package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.connection

import com.inso_world.binocular.infrastructure.arangodb.model.edge.MergeRequestAccountConnection
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IMergeRequestAccountConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.MergeRequestAccountConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.AccountMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.MergeRequestMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.AccountRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.MergeRequestRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges.MergeRequestAccountConnectionRepository
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.MergeRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IMergeRequestAccountConnectionDao.
 *
 * This class adapts the existing MergeRequestAccountConnectionRepository to work with
 * the new domain model and entity structure.
 */

@Repository
internal class MergeRequestAccountConnectionDao
    @Autowired
    constructor(
        private val repository: MergeRequestAccountConnectionRepository,
        private val mergeRequestRepository: MergeRequestRepository,
        private val accountRepository: AccountRepository,
        private val mergeRequestMapper: MergeRequestMapper,
        private val accountMapper: AccountMapper,
    ) : IMergeRequestAccountConnectionDao {
        /**
         * Find all accounts connected to a merge request
         */
        override fun findAccountsByMergeRequest(mergeRequestId: String): List<Account> {
            val accountEntities = repository.findAccountsByMergeRequest(mergeRequestId)
            return accountEntities.map { accountMapper.toDomain(it) }
        }

        /**
         * Find all merge requests connected to an account
         */
        override fun findMergeRequestsByAccount(accountId: String): List<MergeRequest> {
            val mergeRequestEntities = repository.findMergeRequestsByAccount(accountId)
            return mergeRequestEntities.map { mergeRequestMapper.toDomain(it) }
        }

        /**
         * Save a merge request-account connection.
         *
         * Uses the domain objects from [connection] directly on return to avoid requiring
         * an active MappingSession for the mapper round-trip.
         */
        override fun save(connection: MergeRequestAccountConnection): MergeRequestAccountConnection {
            val mergeRequestEntity =
                mergeRequestRepository.findById(connection.from.id!!).orElseThrow {
                    IllegalArgumentException("MergeRequest with ID ${connection.from.id} not found")
                }
            val accountEntity =
                accountRepository.findById(connection.to.id!!).orElseThrow {
                    IllegalArgumentException("Account with ID ${connection.to.id} not found")
                }

            val entity =
                MergeRequestAccountConnectionEntity(
                    id = connection.id,
                    from = mergeRequestEntity,
                    to = accountEntity,
                )

            val savedEntity = repository.save(entity)

            return MergeRequestAccountConnection(
                id = savedEntity.id,
                from = connection.from,
                to = connection.to,
            )
        }

        /**
         * Delete all merge request-account connections
         */
        override fun deleteAll() {
            repository.deleteAll()
        }
    }
