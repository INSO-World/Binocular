package com.inso_world.binocular.web.persistence.dao.nosql.arangodb.connection

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.edge.domain.MergeRequestAccountConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestAccountConnectionDao
import com.inso_world.binocular.web.persistence.entity.arangodb.AccountEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.MergeRequestEntity
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
