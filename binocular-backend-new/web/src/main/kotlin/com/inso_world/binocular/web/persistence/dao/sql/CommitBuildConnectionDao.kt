package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Build
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.edge.domain.CommitBuildConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IBuildDao
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitBuildConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.web.persistence.entity.sql.CommitBuildConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.sql.CommitBuildConnectionMapper
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
class CommitBuildConnectionDao(
    @Autowired private val commitBuildConnectionMapper: CommitBuildConnectionMapper,
    @Autowired private val commitDao: ICommitDao,
    @Autowired private val buildDao: IBuildDao
) : ICommitBuildConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findBuildsByCommit(commitId: String): List<Build> {
        val query = entityManager.createQuery(
            "SELECT b FROM BuildEntity b JOIN CommitBuildConnectionEntity c ON b.id = c.buildId WHERE c.commitId = :commitId",
            com.inso_world.binocular.web.persistence.entity.sql.BuildEntity::class.java
        )
        query.setParameter("commitId", commitId)
        val buildEntities = query.resultList
        
        // Convert SQL entities to domain models
        return buildEntities.map { buildDao.findById(it.id!!)!! }
    }

    override fun findCommitsByBuild(buildId: String): List<Commit> {
        val query = entityManager.createQuery(
            "SELECT c FROM CommitEntity c JOIN CommitBuildConnectionEntity cb ON c.id = cb.commitId WHERE cb.buildId = :buildId",
            com.inso_world.binocular.web.persistence.entity.sql.CommitEntity::class.java
        )
        query.setParameter("buildId", buildId)
        val commitEntities = query.resultList
        
        // Convert SQL entities to domain models
        return commitEntities.map { commitDao.findById(it.id!!)!! }
    }

    override fun save(connection: CommitBuildConnection): CommitBuildConnection {
        val entity = commitBuildConnectionMapper.toEntity(connection)
        
        // Generate ID if not provided
        if (entity.id == null) {
            entity.id = UUID.randomUUID().toString()
        }
        
        // Check if entity already exists
        val existingEntity = entityManager.createQuery(
            "FROM CommitBuildConnectionEntity WHERE commitId = :commitId AND buildId = :buildId",
            CommitBuildConnectionEntity::class.java
        )
            .setParameter("commitId", entity.commitId)
            .setParameter("buildId", entity.buildId)
            .resultList
            .firstOrNull()
        
        if (existingEntity != null) {
            // Update existing entity
            existingEntity.id = entity.id
            val mergedEntity = entityManager.merge(existingEntity)
            return commitBuildConnectionMapper.toDomain(mergedEntity)
        } else {
            // Create new entity
            entityManager.persist(entity)
            return commitBuildConnectionMapper.toDomain(entity)
        }
    }

    override fun deleteAll() {
        entityManager.createQuery("DELETE FROM CommitBuildConnectionEntity").executeUpdate()
    }
}
