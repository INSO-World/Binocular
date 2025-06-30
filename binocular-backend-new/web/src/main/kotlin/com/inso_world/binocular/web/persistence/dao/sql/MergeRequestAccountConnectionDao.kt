package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.edge.domain.MergeRequestAccountConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IAccountDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestAccountConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestDao
import com.inso_world.binocular.web.persistence.entity.sql.MergeRequestAccountConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.sql.MergeRequestAccountConnectionMapper
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
class MergeRequestAccountConnectionDao(
    @Autowired private val mergeRequestAccountConnectionMapper: MergeRequestAccountConnectionMapper,
    @Autowired private val mergeRequestDao: IMergeRequestDao,
    @Autowired private val accountDao: IAccountDao
) : IMergeRequestAccountConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findAccountsByMergeRequest(mergeRequestId: String): List<Account> {
        val query = entityManager.createQuery(
            "SELECT a FROM AccountEntity a JOIN MergeRequestAccountConnectionEntity c ON a.id = c.accountId WHERE c.mergeRequestId = :mergeRequestId",
            com.inso_world.binocular.web.persistence.entity.sql.AccountEntity::class.java
        )
        query.setParameter("mergeRequestId", mergeRequestId)
        val accountEntities = query.resultList
        
        // Convert SQL entities to domain models
        return accountEntities.map { accountDao.findById(it.id!!)!! }
    }

    override fun findMergeRequestsByAccount(accountId: String): List<MergeRequest> {
        val query = entityManager.createQuery(
            "SELECT mr FROM MergeRequestEntity mr JOIN MergeRequestAccountConnectionEntity c ON mr.id = c.mergeRequestId WHERE c.accountId = :accountId",
            com.inso_world.binocular.web.persistence.entity.sql.MergeRequestEntity::class.java
        )
        query.setParameter("accountId", accountId)
        val mergeRequestEntities = query.resultList
        
        // Convert SQL entities to domain models
        return mergeRequestEntities.map { mergeRequestDao.findById(it.id!!)!! }
    }

    override fun save(connection: MergeRequestAccountConnection): MergeRequestAccountConnection {
        val entity = mergeRequestAccountConnectionMapper.toEntity(connection)
        
        // Generate ID if not provided
        if (entity.id == null) {
            entity.id = UUID.randomUUID().toString()
        }
        
        // Check if entity already exists
        val existingEntity = entityManager.createQuery(
            "FROM MergeRequestAccountConnectionEntity WHERE mergeRequestId = :mergeRequestId AND accountId = :accountId",
            MergeRequestAccountConnectionEntity::class.java
        )
            .setParameter("mergeRequestId", entity.mergeRequestId)
            .setParameter("accountId", entity.accountId)
            .resultList
            .firstOrNull()
        
        if (existingEntity != null) {
            // Update existing entity
            existingEntity.id = entity.id
            val mergedEntity = entityManager.merge(existingEntity)
            return mergeRequestAccountConnectionMapper.toDomain(mergedEntity)
        } else {
            // Create new entity
            entityManager.persist(entity)
            return mergeRequestAccountConnectionMapper.toDomain(entity)
        }
    }

    override fun deleteAll() {
        entityManager.createQuery("DELETE FROM MergeRequestAccountConnectionEntity").executeUpdate()
    }
}
