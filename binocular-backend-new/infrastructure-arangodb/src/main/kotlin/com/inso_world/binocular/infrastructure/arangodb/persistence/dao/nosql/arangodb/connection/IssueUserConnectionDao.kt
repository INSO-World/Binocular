package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.connection

import com.inso_world.binocular.infrastructure.arangodb.model.edge.IssueUserConnection
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IIssueUserConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.IssueUserConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.IssueMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.UserMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.IssueRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.UserRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges.IssueUserConnectionRepository
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IIssueUserConnectionDao.
 *
 * This class adapts the existing IssueUserConnectionRepository to work with
 * the new domain model and entity structure.
 */

@Repository
internal class IssueUserConnectionDao
    @Autowired
    constructor(
        private val repository: IssueUserConnectionRepository,
        private val issueRepository: IssueRepository,
        private val userRepository: UserRepository,
        private val issueMapper: IssueMapper,
        private val userMapper: UserMapper,
    ) : IIssueUserConnectionDao {
        /**
         * Find all users connected to an issue
         */
        override fun findUsersByIssue(issueId: String): List<User> {
            val userEntities = repository.findUsersByIssue(issueId)
            return userEntities.map { userMapper.toDomain(it) }
        }

        /**
         * Find all issues connected to a user
         */
        override fun findIssuesByUser(userId: String): List<Issue> {
            val issueEntities = repository.findIssuesByUser(userId)
            return issueEntities.map { issueMapper.toDomain(it) }
        }

        /**
         * Save an issue-user connection
         */
        override fun save(connection: IssueUserConnection): IssueUserConnection {
            // Get the issue and user entities from their repositories
            val issueEntity =
                issueRepository.findById(connection.from.id!!).orElseThrow {
                    IllegalArgumentException("Issue with ID ${connection.from.id} not found")
                }
            val userEntity =
                userRepository.findById(connection.to.id!!).orElseThrow {
                    IllegalArgumentException("User with ID ${connection.to.id} not found")
                }

            // Convert domain model to the entity format
            val entity =
                IssueUserConnectionEntity(
                    id = connection.id,
                    from = issueEntity,
                    to = userEntity,
                )

            // Save using the repository
            val savedEntity = repository.save(entity)

            // Convert back to domain model
            return IssueUserConnection(
                id = savedEntity.id,
                from = issueMapper.toDomain(savedEntity.from),
                to = userMapper.toDomain(savedEntity.to),
            )
        }

        /**
         * Delete all issue-user connections
         */
        override fun deleteAll() {
            repository.deleteAll()
        }
    }
