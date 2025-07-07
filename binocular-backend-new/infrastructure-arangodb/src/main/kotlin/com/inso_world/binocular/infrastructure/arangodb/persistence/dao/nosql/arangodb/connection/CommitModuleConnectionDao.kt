package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.connection

import com.inso_world.binocular.infrastructure.arangodb.model.edge.CommitModuleConnection
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.ICommitModuleConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitModuleConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.ModuleMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.CommitRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.ModuleRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges.CommitModuleConnectionRepository
import com.inso_world.binocular.model.Commit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of ICommitModuleConnectionDao.
 *
 * This class adapts the existing CommitModuleConnectionRepository to work with
 * the new domain model and entity structure.
 */

@Repository
class CommitModuleConnectionDao
    @Autowired
    constructor(
        private val repository: CommitModuleConnectionRepository,
        private val commitRepository: CommitRepository,
        private val moduleRepository: ModuleRepository,
        private val moduleMapper: ModuleMapper,
    ) : ICommitModuleConnectionDao {
        @Autowired private lateinit var commitMapper: CommitMapper

        /**
         * Find all modules connected to a commit
         */
        override fun findModulesByCommit(commitId: String): List<com.inso_world.binocular.model.Module> {
            val moduleEntities = repository.findModulesByCommit(commitId)
            return moduleEntities.map { moduleMapper.toDomain(it) }
        }

        /**
         * Find all commits connected to a module
         */
        override fun findCommitsByModule(moduleId: String): List<Commit> {
            val commitEntities = repository.findCommitsByModule(moduleId)
            return commitEntities.map { commitMapper.toDomain(it) }
        }

        /**
         * Save a commit-module connection
         */
        override fun save(connection: CommitModuleConnection): CommitModuleConnection {
            // Get the commit and module entities from their repositories
            val commitEntity =
                commitRepository.findById(connection.from.id!!).orElseThrow {
                    IllegalArgumentException("Commit with ID ${connection.from.id} not found")
                }
            val moduleEntity =
                moduleRepository.findById(connection.to.id!!).orElseThrow {
                    IllegalArgumentException("Module with ID ${connection.to.id} not found")
                }

            // Convert domain model to the repository entity format
            val entity =
                CommitModuleConnectionEntity(
                    id = connection.id,
                    from = commitEntity,
                    to = moduleEntity,
                )

            // Save using the repository
            val savedEntity = repository.save(entity)

            // Convert back to domain model
            return CommitModuleConnection(
                id = savedEntity.id,
                from = commitMapper.toDomain(savedEntity.from),
                to = moduleMapper.toDomain(savedEntity.to),
            )
        }

        /**
         * Delete all commit-module connections
         */
        override fun deleteAll() {
            repository.deleteAll()
        }
    }
