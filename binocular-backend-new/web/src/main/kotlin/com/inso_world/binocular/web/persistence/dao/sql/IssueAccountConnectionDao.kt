package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.edge.domain.IssueAccountConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IAccountDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueAccountConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueDao
import com.inso_world.binocular.web.persistence.entity.sql.IssueAccountConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.sql.IssueAccountConnectionMapper
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
@Profile("sql")
@Transactional
class IssueAccountConnectionDao(
    @Autowired private val issueAccountConnectionMapper: IssueAccountConnectionMapper,
    @Autowired private val issueDao: IIssueDao,
    @Autowired private val accountDao: IAccountDao
) : IIssueAccountConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findAccountsByIssue(issueId: String): List<Account> {
        val query = entityManager.createQuery(
            "SELECT a FROM AccountEntity a JOIN IssueAccountConnectionEntity c ON a.id = c.accountId WHERE c.issueId = :issueId",
            com.inso_world.binocular.web.persistence.entity.sql.AccountEntity::class.java
        )
        query.setParameter("issueId", issueId)
        val accountEntities = query.resultList
        
        // Convert SQL entities to domain models
        return accountEntities.map { accountDao.findById(it.id!!)!! }
    }

    override fun findIssuesByAccount(accountId: String): List<Issue> {
        val query = entityManager.createQuery(
            "SELECT i FROM IssueEntity i JOIN IssueAccountConnectionEntity c ON i.id = c.issueId WHERE c.accountId = :accountId",
            com.inso_world.binocular.web.persistence.entity.sql.IssueEntity::class.java
        )
        query.setParameter("accountId", accountId)
        val issueEntities = query.resultList
        
        // Convert SQL entities to domain models
        return issueEntities.map { issueDao.findById(it.id!!)!! }
    }

    override fun save(connection: IssueAccountConnection): IssueAccountConnection {
        val entity = issueAccountConnectionMapper.toEntity(connection)
        
        // Generate ID if not provided
        if (entity.id == null) {
            entity.id = UUID.randomUUID().toString()
        }
        
        // Check if entity already exists
        val existingEntity = entityManager.createQuery(
            "FROM IssueAccountConnectionEntity WHERE issueId = :issueId AND accountId = :accountId",
            IssueAccountConnectionEntity::class.java
        )
            .setParameter("issueId", entity.issueId)
            .setParameter("accountId", entity.accountId)
            .resultList
            .firstOrNull()
        
        if (existingEntity != null) {
            // Update existing entity
            existingEntity.id = entity.id
            val mergedEntity = entityManager.merge(existingEntity)
            return issueAccountConnectionMapper.toDomain(mergedEntity)
        } else {
            // Create new entity
            entityManager.persist(entity)
            return issueAccountConnectionMapper.toDomain(entity)
        }
    }

    override fun deleteAll() {
        entityManager.createQuery("DELETE FROM IssueAccountConnectionEntity").executeUpdate()
    }
}
