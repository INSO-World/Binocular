package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.connection

import com.inso_world.binocular.infrastructure.arangodb.model.edge.ModuleModuleConnection
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IModuleModuleConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.ModuleModuleConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.ModuleMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.ModuleRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges.ModuleModuleConnectionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IModuleModuleConnectionDao.
 *
 * This class adapts the existing ModuleModuleConnectionRepository to work with
 * the new domain model and entity structure.
 */

@Repository
class ModuleModuleConnectionDao
    @Autowired
    constructor(
        private val repository: ModuleModuleConnectionRepository,
        private val moduleRepository: ModuleRepository,
        private val moduleMapper: ModuleMapper,
    ) : IModuleModuleConnectionDao {
        /**
         * Find all child modules connected to a parent module
         */
        override fun findChildModules(parentModuleId: String): List<com.inso_world.binocular.model.Module> {
            val moduleEntities = repository.findChildModulesByModule(parentModuleId)
            return moduleEntities.map { moduleMapper.toDomain(it) }
        }

        /**
         * Find all parent modules connected to a child module
         */
        override fun findParentModules(childModuleId: String): List<com.inso_world.binocular.model.Module> {
            val moduleEntities = repository.findParentModulesByModule(childModuleId)
            return moduleEntities.map { moduleMapper.toDomain(it) }
        }

        /**
         * Save a module-module connection
         */
        override fun save(connection: ModuleModuleConnection): ModuleModuleConnection {
            // Get the parent and child module entities from the repository
            val fromModuleEntity =
                moduleRepository.findById(connection.from.id!!).orElseThrow {
                    IllegalArgumentException("Parent Module with ID ${connection.from.id} not found")
                }
            val toModuleEntity =
                moduleRepository.findById(connection.to.id!!).orElseThrow {
                    IllegalArgumentException("Child Module with ID ${connection.to.id} not found")
                }

            // Convert domain model to the entity format
            val entity =
                ModuleModuleConnectionEntity(
                    id = connection.id,
                    from = fromModuleEntity,
                    to = toModuleEntity,
                )

            // Save using the repository
            val savedEntity = repository.save(entity)

            // Convert back to domain model
            return ModuleModuleConnection(
                id = savedEntity.id,
                from = moduleMapper.toDomain(savedEntity.from),
                to = moduleMapper.toDomain(savedEntity.to),
            )
        }

        /**
         * Delete all module-module connections
         */
        override fun deleteAll() {
            repository.deleteAll()
        }
    }
