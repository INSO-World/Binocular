package com.inso_world.binocular.web.persistence.dao.nosql.arangodb.connection

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.edge.domain.IssueCommitConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueCommitConnectionDao
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.IssueCommitConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.arangodb.CommitMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.IssueMapper
import com.inso_world.binocular.web.persistence.repository.arangodb.CommitRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.IssueRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueCommitConnectionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IIssueCommitConnectionDao.
 * 
 * This class adapts the existing IssueCommitConnectionRepository to work with
 * the new domain model and entity structure.
 */
@Repository
@Profile("nosql", "arangodb")
class IssueCommitConnectionDao @Autowired constructor(
    private val repository: IssueCommitConnectionRepository,
    private val issueRepository: IssueRepository,
    private val commitRepository: CommitRepository,
    private val issueMapper: IssueMapper,
    private val commitMapper: CommitMapper
) : IIssueCommitConnectionDao {

    /**
     * Find all commits connected to an issue
     */
    override fun findCommitsByIssue(issueId: String): List<Commit> {
        val commitEntities = repository.findCommitsByIssue(issueId)
        return commitEntities.map { commitMapper.toDomain(it) }
    }

    /**
     * Find all issues connected to a commit
     */
    override fun findIssuesByCommit(commitId: String): List<Issue> {
        val issueEntities = repository.findIssuesByCommit(commitId)
        return issueEntities.map { issueMapper.toDomain(it) }
    }

    /**
     * Save an issue-commit connection
     */
    override fun save(connection: IssueCommitConnection): IssueCommitConnection {
        // Get the issue and commit entities from their repositories
        val issueEntity = issueRepository.findById(connection.from.id!!).orElseThrow { 
            IllegalArgumentException("Issue with ID ${connection.from.id} not found") 
        }
        val commitEntity = commitRepository.findById(connection.to.id!!).orElseThrow { 
            IllegalArgumentException("Commit with ID ${connection.to.id} not found") 
        }

        // Convert domain model to the repository entity format
        val entity = IssueCommitConnectionEntity(
            id = connection.id,
            from = issueEntity,
            to = commitEntity
        )

        // Save using the repository
        val savedEntity = repository.save(entity)

        // Convert back to domain model
        return IssueCommitConnection(
            id = savedEntity.id,
            from = issueMapper.toDomain(savedEntity.from),
            to = commitMapper.toDomain(savedEntity.to)
        )
    }

    /**
     * Delete all issue-commit connections
     */
    override fun deleteAll() {
        repository.deleteAll()
    }
}
