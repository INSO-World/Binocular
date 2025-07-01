package com.inso_world.binocular.web.persistence.dao.nosql.arangodb.connection

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.edge.domain.CommitFileConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitFileConnectionDao
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.CommitFileConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.arangodb.CommitMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.FileMapper
import com.inso_world.binocular.web.persistence.repository.arangodb.CommitRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.FileRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitFileConnectionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of ICommitFileConnectionDao.
 * 
 * This class adapts the existing CommitFileConnectionRepository to work with
 * the new domain model and entity structure.
 */
@Repository
@Profile("nosql", "arangodb")
class CommitFileConnectionDao @Autowired constructor(
    private val repository: CommitFileConnectionRepository,
    private val commitRepository: CommitRepository,
    private val fileRepository: FileRepository,
    private val commitMapper: CommitMapper,
    private val fileMapper: FileMapper
) : ICommitFileConnectionDao {

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
        val commitEntity = commitRepository.findById(connection.from.id!!).orElseThrow { 
            IllegalArgumentException("Commit with ID ${connection.from.id} not found") 
        }
        val fileEntity = fileRepository.findById(connection.to.id!!).orElseThrow { 
            IllegalArgumentException("File with ID ${connection.to.id} not found") 
        }

        // Convert domain model to the repository entity format
        val entity = CommitFileConnectionEntity(
            id = connection.id,
            from = commitEntity,
            to = fileEntity,
            lineCount = connection.lineCount
        )

        // Save using the repository
        val savedEntity = repository.save(entity)

        // Convert back to domain model
        return CommitFileConnection(
            id = savedEntity.id,
            from = commitMapper.toDomain(savedEntity.from),
            to = fileMapper.toDomain(savedEntity.to),
            lineCount = savedEntity.lineCount
        )
    }

    /**
     * Delete all commit-file connections
     */
    override fun deleteAll() {
        repository.deleteAll()
    }
}
