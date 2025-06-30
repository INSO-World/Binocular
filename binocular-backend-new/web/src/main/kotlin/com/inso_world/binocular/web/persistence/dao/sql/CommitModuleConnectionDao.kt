package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.entity.edge.domain.CommitModuleConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitModuleConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IModuleDao
import com.inso_world.binocular.web.persistence.entity.sql.CommitModuleConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.sql.CommitModuleConnectionMapper
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
class CommitModuleConnectionDao(
    @Autowired private val commitModuleConnectionMapper: CommitModuleConnectionMapper,
    @Autowired private val commitDao: ICommitDao,
    @Autowired private val moduleDao: IModuleDao
) : ICommitModuleConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findModulesByCommit(commitId: String): List<Module> {
        val query = entityManager.createQuery(
            "SELECT m FROM ModuleEntity m JOIN CommitModuleConnectionEntity c ON m.id = c.moduleId WHERE c.commitId = :commitId",
            com.inso_world.binocular.web.persistence.entity.sql.ModuleEntity::class.java
        )
        query.setParameter("commitId", commitId)
        val moduleEntities = query.resultList
        
        // Convert SQL entities to domain models
        return moduleEntities.map { moduleDao.findById(it.id!!)!! }
    }

    override fun findCommitsByModule(moduleId: String): List<Commit> {
        val query = entityManager.createQuery(
            "SELECT c FROM CommitEntity c JOIN CommitModuleConnectionEntity cm ON c.id = cm.commitId WHERE cm.moduleId = :moduleId",
            com.inso_world.binocular.web.persistence.entity.sql.CommitEntity::class.java
        )
        query.setParameter("moduleId", moduleId)
        val commitEntities = query.resultList
        
        // Convert SQL entities to domain models
        return commitEntities.map { commitDao.findById(it.id!!)!! }
    }

    override fun save(connection: CommitModuleConnection): CommitModuleConnection {
        val entity = commitModuleConnectionMapper.toEntity(connection)
        
        // Generate ID if not provided
        if (entity.id == null) {
            entity.id = UUID.randomUUID().toString()
        }
        
        // Check if entity already exists
        val existingEntity = entityManager.createQuery(
            "FROM CommitModuleConnectionEntity WHERE commitId = :commitId AND moduleId = :moduleId",
            CommitModuleConnectionEntity::class.java
        )
            .setParameter("commitId", entity.commitId)
            .setParameter("moduleId", entity.moduleId)
            .resultList
            .firstOrNull()
        
        if (existingEntity != null) {
            // Update existing entity
            existingEntity.id = entity.id
            val mergedEntity = entityManager.merge(existingEntity)
            return commitModuleConnectionMapper.toDomain(mergedEntity)
        } else {
            // Create new entity
            entityManager.persist(entity)
            return commitModuleConnectionMapper.toDomain(entity)
        }
    }

    override fun deleteAll() {
        entityManager.createQuery("DELETE FROM CommitModuleConnectionEntity").executeUpdate()
    }
}
