package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.connection

import com.inso_world.binocular.infrastructure.arangodb.model.edge.CommitUserConnection
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.ICommitUserConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitUserConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.UserMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.CommitRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.UserRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges.CommitUserConnectionRepository
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of ICommitUserConnectionDao.
 *
 * This class adapts the existing CommitUserConnectionRepository to work with
 * the new domain model and entity structure.
 */

@Repository
internal class CommitUserConnectionDao
    @Autowired
    constructor(
        private val repository: CommitUserConnectionRepository,
        private val commitRepository: CommitRepository,
        private val userRepository: UserRepository,
        private val userMapper: UserMapper,
    ) : ICommitUserConnectionDao {
        @Autowired private lateinit var commitMapper: CommitMapper

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
            val commitEntity =
                commitRepository.findById(connection.from.id!!).orElseThrow {
                    IllegalArgumentException("Commit with ID ${connection.from.id} not found")
                }
            val userEntity =
                userRepository.findById(connection.to.id!!).orElseThrow {
                    IllegalArgumentException("User with ID ${connection.to.id} not found")
                }

            // Convert domain model to the repository entity format
            val entity =
                CommitUserConnectionEntity(
                    id = connection.id,
                    from = commitEntity,
                    to = userEntity,
                )

            // Save using the repository
            val savedEntity = repository.save(entity)

            // Convert back to domain model
            return CommitUserConnection(
                id = savedEntity.id,
                from = commitMapper.toDomain(savedEntity.from),
                to = userMapper.toDomain(savedEntity.to),
            )
        }

        /**
         * Delete all commit-user connections
         */
        override fun deleteAll() {
            repository.deleteAll()
        }
    }
