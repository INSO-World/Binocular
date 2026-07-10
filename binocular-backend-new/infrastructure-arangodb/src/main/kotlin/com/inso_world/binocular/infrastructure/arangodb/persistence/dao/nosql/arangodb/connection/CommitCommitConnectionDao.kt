package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.connection

import com.inso_world.binocular.infrastructure.arangodb.assembler.RepositoryAssembler
import com.inso_world.binocular.infrastructure.arangodb.model.edge.CommitCommitConnection
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.ICommitCommitConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.DefaultMappingContextSeeder
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitCommitConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.CommitRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges.CommitCommitConnectionRepository
import com.inso_world.binocular.model.Commit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of ICommitCommitConnectionDao.
 *
 * This class adapts the existing CommitCommitConnectionRepository to work with
 * the new domain model and entity structure.
 */

@Repository
class CommitCommitConnectionDao
    @Autowired
    constructor(
        private val repository: CommitCommitConnectionRepository,
        private val commitRepository: CommitRepository,
    ) : ICommitCommitConnectionDao {
        @Autowired private lateinit var commitMapper: CommitMapper

        @Autowired private lateinit var seeder: DefaultMappingContextSeeder

        @Autowired
        private lateinit var repositoryAssembler: RepositoryAssembler

        /**
         * Find all child commits connected to a parent commit
         */
        override fun findChildCommits(parentCommitId: String): List<Commit> {
            seeder.seed()
            val commitEntities = repository.findChildCommitsByParentCommit(parentCommitId)
            return commitEntities.map { entity ->
                repositoryAssembler.toDomain(entity.repository)
                commitMapper.toDomain(entity)
            }
        }

        /**
         * Find all parent commits connected to a child commit
         */
        override fun findParentCommits(childCommitId: String): List<Commit> {
            seeder.seed()
            val commitEntities = repository.findParentCommitsByChildCommit(childCommitId)
            return commitEntities.map { entity ->
                repositoryAssembler.toDomain(entity.repository)
                commitMapper.toDomain(entity)
            }
        }

        /**
         * Save a commit-commit connection.
         *
         * Uses the domain objects from [connection] directly on return to avoid requiring
         * an active MappingSession for the mapper round-trip.
         */
        override fun save(connection: CommitCommitConnection): CommitCommitConnection {
            val fromCommitEntity =
                commitRepository.findById(connection.from.id!!).orElseThrow {
                    IllegalArgumentException("Parent Commit with ID ${connection.from.id} not found")
                }
            val toCommitEntity =
                commitRepository.findById(connection.to.id!!).orElseThrow {
                    IllegalArgumentException("Child Commit with ID ${connection.to.id} not found")
                }

            val entity =
                CommitCommitConnectionEntity(
                    id = connection.id,
                    from = fromCommitEntity,
                    to = toCommitEntity,
                )

            val savedEntity = repository.save(entity)

            return CommitCommitConnection(
                id = savedEntity.id,
                from = connection.from,
                to = connection.to,
            )
        }

        /**
         * Delete all commit-commit connections
         */
        override fun deleteAll() {
            repository.deleteAll()
        }
    }
