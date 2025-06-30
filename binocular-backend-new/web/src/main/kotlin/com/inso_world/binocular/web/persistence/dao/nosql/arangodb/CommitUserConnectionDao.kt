package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.entity.edge.domain.CommitUserConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitUserConnectionDao
import com.inso_world.binocular.web.persistence.entity.arangodb.CommitUserConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.arangodb.CommitMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.UserMapper
import com.inso_world.binocular.web.persistence.repository.arangodb.CommitRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.UserRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitUserConnectionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of ICommitUserConnectionDao.
 * 
 * This class adapts the existing CommitUserConnectionRepository to work with
 * the new domain model and entity structure.
 */
@Repository
@Profile("nosql", "arangodb")
class CommitUserConnectionDao @Autowired constructor(
    private val repository: CommitUserConnectionRepository,
    private val commitRepository: CommitRepository,
    private val userRepository: UserRepository,
    private val commitMapper: CommitMapper,
    private val userMapper: UserMapper
) : ICommitUserConnectionDao {

    /**
     * Find all users connected to a commit
     */
    override fun findUsersByCommit(commitId: String): List<User> {
        val userEntities = repository.findUsersByCommit(commitId)
        return userEntities.map { userMapper.toDomain(it) }
    }

    /**
     * Find all commits connected to a user
     */
    override fun findCommitsByUser(userId: String): List<Commit> {
        val commitEntities = repository.findCommitsByUser(userId)
        return commitEntities.map { commitMapper.toDomain(it) }
    }

    /**
     * Save a commit-user connection
     */
    override fun save(connection: CommitUserConnection): CommitUserConnection {
        // Get the commit and user entities from their repositories
        val commitEntity = commitRepository.findById(connection.from.id!!).orElseThrow { 
            IllegalArgumentException("Commit with ID ${connection.from.id} not found") 
        }
        val userEntity = userRepository.findById(connection.to.id!!).orElseThrow { 
            IllegalArgumentException("User with ID ${connection.to.id} not found") 
        }

        // Convert domain model to the repository entity format
        val entity = CommitUserConnectionEntity(
            id = connection.id,
            from = commitEntity,
            to = userEntity
        )

        // Save using the repository
        val savedEntity = repository.save(entity)

        // Convert back to domain model
        return CommitUserConnection(
            id = savedEntity.id,
            from = commitMapper.toDomain(savedEntity.from),
            to = userMapper.toDomain(savedEntity.to)
        )
    }

    /**
     * Delete all commit-user connections
     */
    override fun deleteAll() {
        repository.deleteAll()
    }
}
