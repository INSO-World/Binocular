package com.inso_world.binocular.web.persistence.dao.sql.connection

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.edge.domain.MergeRequestAccountConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IAccountDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestAccountConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestDao
import com.inso_world.binocular.web.persistence.entity.sql.AccountEntity
import com.inso_world.binocular.web.persistence.entity.sql.MergeRequestEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * SQL implementation of MergeRequestAccountConnectionDao that uses direct JPA relationships
 * instead of intermediate connection entities.
 */
@Repository
@Profile("sql")
@Transactional
class MergeRequestAccountConnectionDao(
    @Autowired private val mergeRequestDao: IMergeRequestDao,
    @Autowired private val accountDao: IAccountDao
) : IMergeRequestAccountConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findAccountsByMergeRequest(mergeRequestId: String): List<Account> {
        // Use the direct relationship between MergeRequest and Account
        val query = entityManager.createQuery(
            "SELECT mr FROM MergeRequestEntity mr WHERE mr.id = :mergeRequestId",
            MergeRequestEntity::class.java
        )
        query.setParameter("mergeRequestId", mergeRequestId)
        val mergeRequestEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return mergeRequestEntity.accounts.map { accountDao.findById(it.id!!)!! }
    }

    override fun findMergeRequestsByAccount(accountId: String): List<MergeRequest> {
        // Use the direct relationship between Account and MergeRequest
        val query = entityManager.createQuery(
            "SELECT a FROM AccountEntity a WHERE a.id = :accountId",
            AccountEntity::class.java
        )
        query.setParameter("accountId", accountId)
        val accountEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return accountEntity.mergeRequests.map { mergeRequestDao.findById(it.id!!)!! }
    }

    override fun save(connection: MergeRequestAccountConnection): MergeRequestAccountConnection {
        val mergeRequestId = connection.from.id ?: throw IllegalStateException("MergeRequest ID cannot be null")
        val accountId = connection.to.id ?: throw IllegalStateException("Account ID cannot be null")

        // Find the entities
        val mergeRequestEntity = entityManager.find(MergeRequestEntity::class.java, mergeRequestId)
            ?: throw IllegalStateException("MergeRequest with ID $mergeRequestId not found")
        val accountEntity = entityManager.find(AccountEntity::class.java, accountId)
            ?: throw IllegalStateException("Account with ID $accountId not found")

        // Add the relationship if it doesn't exist
        if (!mergeRequestEntity.accounts.contains(accountEntity)) {
            mergeRequestEntity.accounts.add(accountEntity)
            entityManager.merge(mergeRequestEntity)
        }

        // Generate a connection ID if not provided
        val connectionId = connection.id ?: UUID.randomUUID().toString()

        // Return the connection with the mergeRequest and account
        return MergeRequestAccountConnection(
            id = connectionId,
            from = mergeRequestDao.findById(mergeRequestId)!!,
            to = accountDao.findById(accountId)!!
        )
    }

    override fun deleteAll() {
        // Clear all relationships between mergeRequests and accounts
        val mergeRequests = entityManager.createQuery("SELECT mr FROM MergeRequestEntity mr", MergeRequestEntity::class.java)
            .resultList
        mergeRequests.forEach { mergeRequest ->
            mergeRequest.accounts.clear()
            entityManager.merge(mergeRequest)
        }
    }
}
