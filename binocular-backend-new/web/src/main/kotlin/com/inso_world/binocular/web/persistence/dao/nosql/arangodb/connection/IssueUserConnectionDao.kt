package com.inso_world.binocular.web.persistence.dao.nosql.arangodb.connection

import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.entity.edge.domain.IssueUserConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueUserConnectionDao
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.IssueUserConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.arangodb.IssueMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.UserMapper
import com.inso_world.binocular.web.persistence.repository.arangodb.IssueRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.UserRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueUserConnectionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IIssueUserConnectionDao.
 * 
 * This class adapts the existing IssueUserConnectionRepository to work with
 * the new domain model and entity structure.
 */
@Repository
@Profile("nosql", "arangodb")
class IssueUserConnectionDao @Autowired constructor(
    private val repository: IssueUserConnectionRepository,
    private val issueRepository: IssueRepository,
    private val userRepository: UserRepository,
    private val issueMapper: IssueMapper,
    private val userMapper: UserMapper
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
        val issueEntity = issueRepository.findById(connection.from.id!!).orElseThrow { 
            IllegalArgumentException("Issue with ID ${connection.from.id} not found") 
        }
        val userEntity = userRepository.findById(connection.to.id!!).orElseThrow { 
            IllegalArgumentException("User with ID ${connection.to.id} not found") 
        }

        // Convert domain model to the entity format
        val entity = IssueUserConnectionEntity(
            id = connection.id,
            from = issueEntity,
            to = userEntity
        )

        // Save using the repository
        val savedEntity = repository.save(entity)

        // Convert back to domain model
        return IssueUserConnection(
            id = savedEntity.id,
            from = issueMapper.toDomain(savedEntity.from),
            to = userMapper.toDomain(savedEntity.to)
        )
    }

    /**
     * Delete all issue-user connections
     */
    override fun deleteAll() {
        repository.deleteAll()
    }
}
