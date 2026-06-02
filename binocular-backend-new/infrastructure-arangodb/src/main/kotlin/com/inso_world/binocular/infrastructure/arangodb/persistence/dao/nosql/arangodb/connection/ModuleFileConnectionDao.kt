package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.connection

import com.inso_world.binocular.infrastructure.arangodb.model.edge.ModuleFileConnection
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IModuleFileConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.ModuleFileConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.FileMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.ModuleMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.FileRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.ModuleRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges.ModuleFileConnectionRepository
import com.inso_world.binocular.model.File
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IModuleFileConnectionDao.
 *
 * This class adapts the existing ModuleFileConnectionRepository to work with
 * the new domain model and entity structure.
 */

@Repository
internal class ModuleFileConnectionDao
    @Autowired
    constructor(
        private val repository: ModuleFileConnectionRepository,
        private val moduleRepository: ModuleRepository,
        private val fileRepository: FileRepository,
        private val moduleMapper: ModuleMapper,
        private val fileMapper: FileMapper,
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
        override fun findModulesByFile(fileId: String): List<com.inso_world.binocular.model.Module> {
            val moduleEntities = repository.findModulesByFile(fileId)
            return moduleEntities.map { moduleMapper.toDomain(it) }
        }

        /**
         * Save a module-file connection.
         *
         * Uses the domain objects from [connection] directly on return to avoid requiring
         * an active MappingSession for the mapper round-trip.
         */
        override fun save(connection: ModuleFileConnection): ModuleFileConnection {
            val moduleEntity =
                moduleRepository.findById(connection.from.id!!).orElseThrow {
                    IllegalArgumentException("Module with ID ${connection.from.id} not found")
                }
            val fileEntity =
                fileRepository.findById(connection.to.id!!).orElseThrow {
                    IllegalArgumentException("File with ID ${connection.to.id} not found")
                }

            val entity =
                ModuleFileConnectionEntity(
                    id = connection.id,
                    from = moduleEntity,
                    to = fileEntity,
                )

            val savedEntity = repository.save(entity)

            return ModuleFileConnection(
                id = savedEntity.id,
                from = connection.from,
                to = connection.to,
            )
        }

        /**
         * Delete all module-file connections
         */
        override fun deleteAll() {
            repository.deleteAll()
        }
    }
