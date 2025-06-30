package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.entity.edge.domain.ModuleFileConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IModuleFileConnectionDao
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.ModuleFileConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.arangodb.FileMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.ModuleMapper
import com.inso_world.binocular.web.persistence.repository.arangodb.FileRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.ModuleRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.ModuleFileConnectionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IModuleFileConnectionDao.
 * 
 * This class adapts the existing ModuleFileConnectionRepository to work with
 * the new domain model and entity structure.
 */
@Repository
@Profile("nosql", "arangodb")
class ModuleFileConnectionDao @Autowired constructor(
    private val repository: ModuleFileConnectionRepository,
    private val moduleRepository: ModuleRepository,
    private val fileRepository: FileRepository,
    private val moduleMapper: ModuleMapper,
    private val fileMapper: FileMapper
) : IModuleFileConnectionDao {

    /**
     * Find all files connected to a module
     */
    override fun findFilesByModule(moduleId: String): List<File> {
        val fileEntities = repository.findFilesByModule(moduleId)
        return fileEntities.map { fileMapper.toDomain(it) }
    }

    /**
     * Find all modules connected to a file
     */
    override fun findModulesByFile(fileId: String): List<Module> {
        val moduleEntities = repository.findModulesByFile(fileId)
        return moduleEntities.map { moduleMapper.toDomain(it) }
    }

    /**
     * Save a module-file connection
     */
    override fun save(connection: ModuleFileConnection): ModuleFileConnection {
        // Get the module and file entities from their repositories
        val moduleEntity = moduleRepository.findById(connection.from.id!!).orElseThrow { 
            IllegalArgumentException("Module with ID ${connection.from.id} not found") 
        }
        val fileEntity = fileRepository.findById(connection.to.id!!).orElseThrow { 
            IllegalArgumentException("File with ID ${connection.to.id} not found") 
        }

        // Convert domain model to the entity format
        val entity = ModuleFileConnectionEntity(
            id = connection.id,
            from = moduleEntity,
            to = fileEntity
        )

        // Save using the repository
        val savedEntity = repository.save(entity)

        // Convert back to domain model
        return ModuleFileConnection(
            id = savedEntity.id,
            from = moduleMapper.toDomain(savedEntity.from),
            to = fileMapper.toDomain(savedEntity.to)
        )
    }

    /**
     * Delete all module-file connections
     */
    override fun deleteAll() {
        repository.deleteAll()
    }
}
