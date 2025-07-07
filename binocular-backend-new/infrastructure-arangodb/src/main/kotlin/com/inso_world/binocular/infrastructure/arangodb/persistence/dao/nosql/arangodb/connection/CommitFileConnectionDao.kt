package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.connection

import com.inso_world.binocular.infrastructure.arangodb.model.edge.CommitFileConnection
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.ICommitFileConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitFileConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.FileMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.CommitRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.FileRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges.CommitFileConnectionRepository
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of ICommitFileConnectionDao.
 *
 * This class adapts the existing CommitFileConnectionRepository to work with
 * the new domain model and entity structure.
 */

@Repository
class CommitFileConnectionDao
    @Autowired
    constructor(
        private val repository: CommitFileConnectionRepository,
        private val commitRepository: CommitRepository,
        private val fileRepository: FileRepository,
        private val fileMapper: FileMapper,
    ) : ICommitFileConnectionDao {
        @Autowired private lateinit var commitMapper: CommitMapper

        /**
         * Find all files connected to a commit
         */
        override fun findFilesByCommit(commitId: String): List<File> {
            val fileEntities = repository.findFilesByCommit(commitId)
            return fileEntities.map { fileMapper.toDomain(it) }
        }

        /**
         * Find all commits connected to a file
         */
        override fun findCommitsByFile(fileId: String): List<Commit> {
            val commitEntities = repository.findCommitsByFile(fileId)
            return commitEntities.map { commitMapper.toDomain(it) }
        }

        /**
         * Save a commit-file connection
         */
        override fun save(connection: CommitFileConnection): CommitFileConnection {
            // Get the commit and file entities from their repositories
            val commitEntity =
                commitRepository.findById(connection.from.id!!).orElseThrow {
                    IllegalArgumentException("Commit with ID ${connection.from.id} not found")
                }
            val fileEntity =
                fileRepository.findById(connection.to.id!!).orElseThrow {
                    IllegalArgumentException("File with ID ${connection.to.id} not found")
                }

            // Convert domain model to the repository entity format
            val entity =
                CommitFileConnectionEntity(
                    id = connection.id,
                    from = commitEntity,
                    to = fileEntity,
                    lineCount = connection.lineCount,
                )

            // Save using the repository
            val savedEntity = repository.save(entity)

            // Convert back to domain model
            return CommitFileConnection(
                id = savedEntity.id,
                from = commitMapper.toDomain(savedEntity.from),
                to = fileMapper.toDomain(savedEntity.to),
                lineCount = savedEntity.lineCount,
            )
        }

        /**
         * Delete all commit-file connections
         */
        override fun deleteAll() {
            repository.deleteAll()
        }
    }
