package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.edge.domain.IssueAccountConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IAccountDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueAccountConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueDao
import com.inso_world.binocular.web.persistence.entity.sql.AccountEntity
import com.inso_world.binocular.web.persistence.entity.sql.IssueEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * SQL implementation of IssueAccountConnectionDao that uses direct JPA relationships
 * instead of intermediate connection entities.
 */
@Repository
@Profile("sql")
@Transactional
class IssueAccountConnectionDao(
    @Autowired private val issueDao: IIssueDao,
    @Autowired private val accountDao: IAccountDao
) : IIssueAccountConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findAccountsByIssue(issueId: String): List<Account> {
        // Use the direct relationship between Issue and Account
        val query = entityManager.createQuery(
            "SELECT i FROM IssueEntity i WHERE i.id = :issueId",
            IssueEntity::class.java
        )
        query.setParameter("issueId", issueId)
        val issueEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return issueEntity.accounts.map { accountDao.findById(it.id!!)!! }
    }

    override fun findIssuesByAccount(accountId: String): List<Issue> {
        // Use the direct relationship between Account and Issue
        val query = entityManager.createQuery(
            "SELECT a FROM AccountEntity a WHERE a.id = :accountId",
            AccountEntity::class.java
        )
        query.setParameter("accountId", accountId)
        val accountEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return accountEntity.issues.map { issueDao.findById(it.id!!)!! }
    }

    override fun save(connection: IssueAccountConnection): IssueAccountConnection {
        val issueId = connection.from.id ?: throw IllegalStateException("Issue ID cannot be null")
        val accountId = connection.to.id ?: throw IllegalStateException("Account ID cannot be null")

        // Find the entities
        val issueEntity = entityManager.find(IssueEntity::class.java, issueId)
            ?: throw IllegalStateException("Issue with ID $issueId not found")
        val accountEntity = entityManager.find(AccountEntity::class.java, accountId)
            ?: throw IllegalStateException("Account with ID $accountId not found")

        // Add the relationship if it doesn't exist
        if (!issueEntity.accounts.contains(accountEntity)) {
            issueEntity.accounts.add(accountEntity)
            entityManager.merge(issueEntity)
        }

        // Generate a connection ID if not provided
        val connectionId = connection.id ?: UUID.randomUUID().toString()

        // Return the connection with the issue and account
        return IssueAccountConnection(
            id = connectionId,
            from = issueDao.findById(issueId)!!,
            to = accountDao.findById(accountId)!!
        )
    }

    override fun deleteAll() {
        // Clear all relationships between issues and accounts
        val issues = entityManager.createQuery("FROM IssueEntity", IssueEntity::class.java).resultList
        for (issue in issues) {
            issue.accounts.clear()
            entityManager.merge(issue)
        }
    }
}
