package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.connection

import com.inso_world.binocular.infrastructure.arangodb.model.edge.CommitCommitConnection
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.ICommitCommitConnectionDao
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

        /**
         * Find all child commits connected to a parent commit
         */
        override fun findChildCommits(parentCommitId: String): List<Commit> {
            val commitEntities = repository.findChildCommitsByParentCommit(parentCommitId)
            return commitEntities.map { commitMapper.toDomain(it) }
        }

        /**
         * Find all parent commits connected to a child commit
         */
        override fun findParentCommits(childCommitId: String): List<Commit> {
            val commitEntities = repository.findParentCommitsByChildCommit(childCommitId)
            return commitEntities.map { commitMapper.toDomain(it) }
        }

        /**
         * Save a commit-commit connection
         */
        override fun save(connection: CommitCommitConnection): CommitCommitConnection {
            // Get the parent and child commit entities from the repository
            val fromCommitEntity =
                commitRepository.findById(connection.from.id!!).orElseThrow {
                    IllegalArgumentException("Parent Commit with ID ${connection.from.id} not found")
                }
            val toCommitEntity =
                commitRepository.findById(connection.to.id!!).orElseThrow {
                    IllegalArgumentException("Child Commit with ID ${connection.to.id} not found")
                }

            // Convert domain model to the repository entity format
            val entity =
                CommitCommitConnectionEntity(
                    id = connection.id,
                    from = fromCommitEntity,
                    to = toCommitEntity,
                )

            // Save using the repository
            val savedEntity = repository.save(entity)

            // Convert back to domain model
            return CommitCommitConnection(
                id = savedEntity.id,
                from = commitMapper.toDomain(savedEntity.from),
                to = commitMapper.toDomain(savedEntity.to),
            )
        }

        /**
         * Delete all commit-commit connections
         */
        override fun deleteAll() {
            repository.deleteAll()
        }
    }
