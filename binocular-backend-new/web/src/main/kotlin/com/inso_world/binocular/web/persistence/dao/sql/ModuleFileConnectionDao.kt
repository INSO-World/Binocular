package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.entity.edge.domain.ModuleFileConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IFileDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IModuleDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IModuleFileConnectionDao
import com.inso_world.binocular.web.persistence.entity.sql.ModuleFileConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.sql.ModuleFileConnectionMapper
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
class ModuleFileConnectionDao(
    @Autowired private val moduleFileConnectionMapper: ModuleFileConnectionMapper,
    @Autowired private val moduleDao: IModuleDao,
    @Autowired private val fileDao: IFileDao
) : IModuleFileConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findFilesByModule(moduleId: String): List<File> {
        val query = entityManager.createQuery(
            "SELECT f FROM FileEntity f JOIN ModuleFileConnectionEntity c ON f.id = c.fileId WHERE c.moduleId = :moduleId",
            com.inso_world.binocular.web.persistence.entity.sql.FileEntity::class.java
        )
        query.setParameter("moduleId", moduleId)
        val fileEntities = query.resultList
        
        // Convert SQL entities to domain models
        return fileEntities.map { fileDao.findById(it.id!!)!! }
    }

    override fun findModulesByFile(fileId: String): List<Module> {
        val query = entityManager.createQuery(
            "SELECT m FROM ModuleEntity m JOIN ModuleFileConnectionEntity c ON m.id = c.moduleId WHERE c.fileId = :fileId",
            com.inso_world.binocular.web.persistence.entity.sql.ModuleEntity::class.java
        )
        query.setParameter("fileId", fileId)
        val moduleEntities = query.resultList
        
        // Convert SQL entities to domain models
        return moduleEntities.map { moduleDao.findById(it.id!!)!! }
    }

    override fun save(connection: ModuleFileConnection): ModuleFileConnection {
        val entity = moduleFileConnectionMapper.toEntity(connection)
        
        // Generate ID if not provided
        if (entity.id == null) {
            entity.id = UUID.randomUUID().toString()
        }
        
        // Check if entity already exists
        val existingEntity = entityManager.createQuery(
            "FROM ModuleFileConnectionEntity WHERE moduleId = :moduleId AND fileId = :fileId",
            ModuleFileConnectionEntity::class.java
        )
            .setParameter("moduleId", entity.moduleId)
            .setParameter("fileId", entity.fileId)
            .resultList
            .firstOrNull()
        
        if (existingEntity != null) {
            // Update existing entity
            existingEntity.id = entity.id
            val mergedEntity = entityManager.merge(existingEntity)
            return moduleFileConnectionMapper.toDomain(mergedEntity)
        } else {
            // Create new entity
            entityManager.persist(entity)
            return moduleFileConnectionMapper.toDomain(entity)
        }
    }

    override fun deleteAll() {
        entityManager.createQuery("DELETE FROM ModuleFileConnectionEntity").executeUpdate()
    }
}
