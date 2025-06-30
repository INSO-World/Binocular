package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.entity.edge.domain.CommitUserConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitUserConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IUserDao
import com.inso_world.binocular.web.persistence.entity.sql.CommitUserConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.sql.CommitUserConnectionMapper
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
class CommitUserConnectionDao(
    @Autowired private val commitUserConnectionMapper: CommitUserConnectionMapper,
    @Autowired private val commitDao: ICommitDao,
    @Autowired private val userDao: IUserDao
) : ICommitUserConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findUsersByCommit(commitId: String): List<User> {
        val query = entityManager.createQuery(
            "SELECT u FROM UserEntity u JOIN CommitUserConnectionEntity c ON u.id = c.userId WHERE c.commitId = :commitId",
            com.inso_world.binocular.web.persistence.entity.sql.UserEntity::class.java
        )
        query.setParameter("commitId", commitId)
        val userEntities = query.resultList
        
        // Convert SQL entities to domain models
        return userEntities.map { userDao.findById(it.id!!)!! }
    }

    override fun findCommitsByUser(userId: String): List<Commit> {
        val query = entityManager.createQuery(
            "SELECT c FROM CommitEntity c JOIN CommitUserConnectionEntity cu ON c.id = cu.commitId WHERE cu.userId = :userId",
            com.inso_world.binocular.web.persistence.entity.sql.CommitEntity::class.java
        )
        query.setParameter("userId", userId)
        val commitEntities = query.resultList
        
        // Convert SQL entities to domain models
        return commitEntities.map { commitDao.findById(it.id!!)!! }
    }

    override fun save(connection: CommitUserConnection): CommitUserConnection {
        val entity = commitUserConnectionMapper.toEntity(connection)
        
        // Generate ID if not provided
        if (entity.id == null) {
            entity.id = UUID.randomUUID().toString()
        }
        
        // Check if entity already exists
        val existingEntity = entityManager.createQuery(
            "FROM CommitUserConnectionEntity WHERE commitId = :commitId AND userId = :userId",
            CommitUserConnectionEntity::class.java
        )
            .setParameter("commitId", entity.commitId)
            .setParameter("userId", entity.userId)
            .resultList
            .firstOrNull()
        
        if (existingEntity != null) {
            // Update existing entity
            existingEntity.id = entity.id
            val mergedEntity = entityManager.merge(existingEntity)
            return commitUserConnectionMapper.toDomain(mergedEntity)
        } else {
            // Create new entity
            entityManager.persist(entity)
            return commitUserConnectionMapper.toDomain(entity)
        }
    }

    override fun deleteAll() {
        entityManager.createQuery("DELETE FROM CommitUserConnectionEntity").executeUpdate()
    }
}
