package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.entity.edge.domain.ModuleFileConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IFileDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IModuleDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IModuleFileConnectionDao
import com.inso_world.binocular.web.persistence.entity.sql.FileEntity
import com.inso_world.binocular.web.persistence.entity.sql.ModuleEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * SQL implementation of ModuleFileConnectionDao that uses direct JPA relationships
 * instead of intermediate connection entities.
 */
@Repository
@Profile("sql")
@Transactional
class ModuleFileConnectionDao(
    @Autowired private val moduleDao: IModuleDao,
    @Autowired private val fileDao: IFileDao
) : IModuleFileConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findFilesByModule(moduleId: String): List<File> {
        // Use the direct relationship between Module and File
        val query = entityManager.createQuery(
            "SELECT m FROM ModuleEntity m WHERE m.id = :moduleId",
            ModuleEntity::class.java
        )
        query.setParameter("moduleId", moduleId)
        val moduleEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return moduleEntity.files.map { fileDao.findById(it.id!!)!! }
    }

    override fun findModulesByFile(fileId: String): List<Module> {
        // Use the direct relationship between File and Module
        val query = entityManager.createQuery(
            "SELECT f FROM FileEntity f WHERE f.id = :fileId",
            FileEntity::class.java
        )
        query.setParameter("fileId", fileId)
        val fileEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return fileEntity.modules.map { moduleDao.findById(it.id!!)!! }
    }

    override fun save(connection: ModuleFileConnection): ModuleFileConnection {
        val moduleId = connection.from.id ?: throw IllegalStateException("Module ID cannot be null")
        val fileId = connection.to.id ?: throw IllegalStateException("File ID cannot be null")

        // Find the entities
        val moduleEntity = entityManager.find(ModuleEntity::class.java, moduleId)
            ?: throw IllegalStateException("Module with ID $moduleId not found")
        val fileEntity = entityManager.find(FileEntity::class.java, fileId)
            ?: throw IllegalStateException("File with ID $fileId not found")

        // Add the relationship if it doesn't exist
        if (!fileEntity.modules.contains(moduleEntity)) {
            fileEntity.modules.add(moduleEntity)
            entityManager.merge(fileEntity)
        }

        // Generate a connection ID if not provided
        val connectionId = connection.id ?: UUID.randomUUID().toString()

        // Return the connection with the module and file
        return ModuleFileConnection(
            id = connectionId,
            from = moduleDao.findById(moduleId)!!,
            to = fileDao.findById(fileId)!!
        )
    }

    override fun deleteAll() {
        // Clear all relationships between modules and files
        val files = entityManager.createQuery("FROM FileEntity", FileEntity::class.java).resultList
        for (file in files) {
            file.modules.clear()
            entityManager.merge(file)
        }
    }
}
