package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.entity.edge.domain.ModuleModuleConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IModuleDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IModuleModuleConnectionDao
import com.inso_world.binocular.web.persistence.entity.sql.ModuleModuleConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.sql.ModuleModuleConnectionMapper
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
class ModuleModuleConnectionDao(
    @Autowired private val moduleModuleConnectionMapper: ModuleModuleConnectionMapper,
    @Autowired private val moduleDao: IModuleDao
) : IModuleModuleConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findChildModules(parentModuleId: String): List<Module> {
        val query = entityManager.createQuery(
            "SELECT m FROM ModuleEntity m JOIN ModuleModuleConnectionEntity c ON m.id = c.toModuleId WHERE c.fromModuleId = :parentModuleId",
            com.inso_world.binocular.web.persistence.entity.sql.ModuleEntity::class.java
        )
        query.setParameter("parentModuleId", parentModuleId)
        val moduleEntities = query.resultList
        
        // Convert SQL entities to domain models
        return moduleEntities.map { moduleDao.findById(it.id!!)!! }
    }

    override fun findParentModules(childModuleId: String): List<Module> {
        val query = entityManager.createQuery(
            "SELECT m FROM ModuleEntity m JOIN ModuleModuleConnectionEntity c ON m.id = c.fromModuleId WHERE c.toModuleId = :childModuleId",
            com.inso_world.binocular.web.persistence.entity.sql.ModuleEntity::class.java
        )
        query.setParameter("childModuleId", childModuleId)
        val moduleEntities = query.resultList
        
        // Convert SQL entities to domain models
        return moduleEntities.map { moduleDao.findById(it.id!!)!! }
    }

    override fun save(connection: ModuleModuleConnection): ModuleModuleConnection {
        val entity = moduleModuleConnectionMapper.toEntity(connection)
        
        // Generate ID if not provided
        if (entity.id == null) {
            entity.id = UUID.randomUUID().toString()
        }
        
        // Check if entity already exists
        val existingEntity = entityManager.createQuery(
            "FROM ModuleModuleConnectionEntity WHERE fromModuleId = :fromModuleId AND toModuleId = :toModuleId",
            ModuleModuleConnectionEntity::class.java
        )
            .setParameter("fromModuleId", entity.fromModuleId)
            .setParameter("toModuleId", entity.toModuleId)
            .resultList
            .firstOrNull()
        
        if (existingEntity != null) {
            // Update existing entity
            existingEntity.id = entity.id
            val mergedEntity = entityManager.merge(existingEntity)
            return moduleModuleConnectionMapper.toDomain(mergedEntity)
        } else {
            // Create new entity
            entityManager.persist(entity)
            return moduleModuleConnectionMapper.toDomain(entity)
        }
    }

    override fun deleteAll() {
        entityManager.createQuery("DELETE FROM ModuleModuleConnectionEntity").executeUpdate()
    }
}
