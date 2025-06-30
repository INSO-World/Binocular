package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.edge.domain.MergeRequestAccountConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestAccountConnectionDao
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.MergeRequestAccountConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.arangodb.AccountMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.MergeRequestMapper
import com.inso_world.binocular.web.persistence.repository.arangodb.AccountRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.MergeRequestRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.MergeRequestAccountConnectionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IMergeRequestAccountConnectionDao.
 * 
 * This class adapts the existing MergeRequestAccountConnectionRepository to work with
 * the new domain model and entity structure.
 */
@Repository
@Profile("nosql", "arangodb")
class MergeRequestAccountConnectionDao @Autowired constructor(
    private val repository: MergeRequestAccountConnectionRepository,
    private val mergeRequestRepository: MergeRequestRepository,
    private val accountRepository: AccountRepository,
    private val mergeRequestMapper: MergeRequestMapper,
    private val accountMapper: AccountMapper
) : IMergeRequestAccountConnectionDao {

    /**
     * Find all accounts connected to a merge request
     */
    override fun findAccountsByMergeRequest(mergeRequestId: String): List<Account> {
        val accounts = repository.findAccountsByMergeRequest(mergeRequestId)
        // If the list is empty or the first item is already a domain entity, return as is
        if (accounts.isEmpty() || accounts[0] is Account) {
            return accounts as List<Account>
        }
        // Otherwise, map the database entities to domain entities
        return accounts.map { accountMapper.toDomain(it as com.inso_world.binocular.web.persistence.entity.arangodb.AccountEntity) }
    }

    /**
     * Find all merge requests connected to an account
     */
    override fun findMergeRequestsByAccount(accountId: String): List<MergeRequest> {
        val mergeRequests = repository.findMergeRequestsByAccount(accountId)
        // If the list is empty or the first item is already a domain entity, return as is
        if (mergeRequests.isEmpty() || mergeRequests[0] is MergeRequest) {
            return mergeRequests as List<MergeRequest>
        }
        // Otherwise, map the database entities to domain entities
        return mergeRequests.map { mergeRequestMapper.toDomain(it as com.inso_world.binocular.web.persistence.entity.arangodb.MergeRequestEntity) }
    }

    /**
     * Save a merge request-account connection
     */
    override fun save(connection: MergeRequestAccountConnection): MergeRequestAccountConnection {
        // Get the merge request and account entities from their repositories
        val mergeRequestEntity = mergeRequestRepository.findById(connection.from.id!!).orElseThrow { 
            IllegalArgumentException("MergeRequest with ID ${connection.from.id} not found") 
        }
        val accountEntity = accountRepository.findById(connection.to.id!!).orElseThrow { 
            IllegalArgumentException("Account with ID ${connection.to.id} not found") 
        }

        // Convert domain model to the entity format
        val entity = MergeRequestAccountConnectionEntity(
            id = connection.id,
            from = mergeRequestEntity,
            to = accountEntity
        )

        // Save using the repository
        val savedEntity = repository.save(entity)

        // Convert back to domain model
        return MergeRequestAccountConnection(
            id = savedEntity.id,
            from = mergeRequestMapper.toDomain(savedEntity.from),
            to = accountMapper.toDomain(savedEntity.to)
        )
    }

    /**
     * Delete all merge request-account connections
     */
    override fun deleteAll() {
        repository.deleteAll()
    }
}
