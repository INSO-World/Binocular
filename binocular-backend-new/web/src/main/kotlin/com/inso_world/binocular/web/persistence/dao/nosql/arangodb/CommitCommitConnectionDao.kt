package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.edge.domain.CommitCommitConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitCommitConnectionDao
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.CommitCommitConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.arangodb.CommitMapper
import com.inso_world.binocular.web.persistence.repository.arangodb.CommitRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitCommitConnectionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of ICommitCommitConnectionDao.
 * 
 * This class adapts the existing CommitCommitConnectionRepository to work with
 * the new domain model and entity structure.
 */
@Repository
@Profile("nosql", "arangodb")
class CommitCommitConnectionDao @Autowired constructor(
    private val repository: CommitCommitConnectionRepository,
    private val commitRepository: CommitRepository,
    private val commitMapper: CommitMapper
) : ICommitCommitConnectionDao {

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
        val fromCommitEntity = commitRepository.findById(connection.from.id!!).orElseThrow { 
            IllegalArgumentException("Parent Commit with ID ${connection.from.id} not found") 
        }
        val toCommitEntity = commitRepository.findById(connection.to.id!!).orElseThrow { 
            IllegalArgumentException("Child Commit with ID ${connection.to.id} not found") 
        }

        // Convert domain model to the repository entity format
        val entity = CommitCommitConnectionEntity(
            id = connection.id,
            from = fromCommitEntity,
            to = toCommitEntity
        )

        // Save using the repository
        val savedEntity = repository.save(entity)

        // Convert back to domain model
        return CommitCommitConnection(
            id = savedEntity.id,
            from = commitMapper.toDomain(savedEntity.from),
            to = commitMapper.toDomain(savedEntity.to)
        )
    }

    /**
     * Delete all commit-commit connections
     */
    override fun deleteAll() {
        repository.deleteAll()
    }
}
