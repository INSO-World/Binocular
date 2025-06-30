package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.entity.edge.domain.IssueUserConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueUserConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IUserDao
import com.inso_world.binocular.web.persistence.entity.sql.IssueUserConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.sql.IssueUserConnectionMapper
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
class IssueUserConnectionDao(
    @Autowired private val issueUserConnectionMapper: IssueUserConnectionMapper,
    @Autowired private val issueDao: IIssueDao,
    @Autowired private val userDao: IUserDao
) : IIssueUserConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findUsersByIssue(issueId: String): List<User> {
        val query = entityManager.createQuery(
            "SELECT u FROM UserEntity u JOIN IssueUserConnectionEntity iuc ON u.id = iuc.userId WHERE iuc.issueId = :issueId",
            com.inso_world.binocular.web.persistence.entity.sql.UserEntity::class.java
        )
        query.setParameter("issueId", issueId)
        val userEntities = query.resultList
        
        // Convert SQL entities to domain models
        return userEntities.map { userDao.findById(it.id!!)!! }
    }

    override fun findIssuesByUser(userId: String): List<Issue> {
        val query = entityManager.createQuery(
            "SELECT i FROM IssueEntity i JOIN IssueUserConnectionEntity iuc ON i.id = iuc.issueId WHERE iuc.userId = :userId",
            com.inso_world.binocular.web.persistence.entity.sql.IssueEntity::class.java
        )
        query.setParameter("userId", userId)
        val issueEntities = query.resultList
        
        // Convert SQL entities to domain models
        return issueEntities.map { issueDao.findById(it.id!!)!! }
    }

    override fun save(connection: IssueUserConnection): IssueUserConnection {
        val entity = issueUserConnectionMapper.toEntity(connection)
        
        // Generate ID if not provided
        if (entity.id == null) {
            entity.id = UUID.randomUUID().toString()
        }
        
        // Check if entity already exists
        val existingEntity = entityManager.createQuery(
            "FROM IssueUserConnectionEntity WHERE issueId = :issueId AND userId = :userId",
            IssueUserConnectionEntity::class.java
        )
            .setParameter("issueId", entity.issueId)
            .setParameter("userId", entity.userId)
            .resultList
            .firstOrNull()
        
        if (existingEntity != null) {
            // Update existing entity
            existingEntity.id = entity.id
            val mergedEntity = entityManager.merge(existingEntity)
            return issueUserConnectionMapper.toDomain(mergedEntity)
        } else {
            // Create new entity
            entityManager.persist(entity)
            return issueUserConnectionMapper.toDomain(entity)
        }
    }

    override fun deleteAll() {
        entityManager.createQuery("DELETE FROM IssueUserConnectionEntity").executeUpdate()
    }
}
