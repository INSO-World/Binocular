package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.edge.domain.CommitCommitConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitCommitConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.web.persistence.entity.sql.CommitCommitConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.sql.CommitCommitConnectionMapper
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
class CommitCommitConnectionDao(
    @Autowired private val commitCommitConnectionMapper: CommitCommitConnectionMapper,
    @Autowired private val commitDao: ICommitDao
) : ICommitCommitConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findChildCommits(parentCommitId: String): List<Commit> {
        val query = entityManager.createQuery(
            "SELECT c FROM CommitEntity c JOIN CommitCommitConnectionEntity cc ON c.id = cc.fromCommitId WHERE cc.toCommitId = :parentCommitId",
            com.inso_world.binocular.web.persistence.entity.sql.CommitEntity::class.java
        )
        query.setParameter("parentCommitId", parentCommitId)
        val commitEntities = query.resultList
        
        // Convert SQL entities to domain models
        return commitEntities.map { commitDao.findById(it.id!!)!! }
    }

    override fun findParentCommits(childCommitId: String): List<Commit> {
        val query = entityManager.createQuery(
            "SELECT c FROM CommitEntity c JOIN CommitCommitConnectionEntity cc ON c.id = cc.toCommitId WHERE cc.fromCommitId = :childCommitId",
            com.inso_world.binocular.web.persistence.entity.sql.CommitEntity::class.java
        )
        query.setParameter("childCommitId", childCommitId)
        val commitEntities = query.resultList
        
        // Convert SQL entities to domain models
        return commitEntities.map { commitDao.findById(it.id!!)!! }
    }

    override fun save(connection: CommitCommitConnection): CommitCommitConnection {
        val entity = commitCommitConnectionMapper.toEntity(connection)
        
        // Generate ID if not provided
        if (entity.id == null) {
            entity.id = UUID.randomUUID().toString()
        }
        
        // Check if entity already exists
        val existingEntity = entityManager.createQuery(
            "FROM CommitCommitConnectionEntity WHERE fromCommitId = :fromCommitId AND toCommitId = :toCommitId",
            CommitCommitConnectionEntity::class.java
        )
            .setParameter("fromCommitId", entity.fromCommitId)
            .setParameter("toCommitId", entity.toCommitId)
            .resultList
            .firstOrNull()
        
        if (existingEntity != null) {
            // Update existing entity
            existingEntity.id = entity.id
            val mergedEntity = entityManager.merge(existingEntity)
            return commitCommitConnectionMapper.toDomain(mergedEntity)
        } else {
            // Create new entity
            entityManager.persist(entity)
            return commitCommitConnectionMapper.toDomain(entity)
        }
    }

    override fun deleteAll() {
        entityManager.createQuery("DELETE FROM CommitCommitConnectionEntity").executeUpdate()
    }
}
