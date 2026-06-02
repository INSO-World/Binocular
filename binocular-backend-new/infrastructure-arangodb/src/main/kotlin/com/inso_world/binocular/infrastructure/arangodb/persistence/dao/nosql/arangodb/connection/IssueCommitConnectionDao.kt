package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.connection

import com.inso_world.binocular.infrastructure.arangodb.model.edge.IssueCommitConnection
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IIssueCommitConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.IssueCommitConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.IssueMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.CommitRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.IssueRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges.IssueCommitConnectionRepository
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Issue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IIssueCommitConnectionDao.
 *
 * This class adapts the existing IssueCommitConnectionRepository to work with
 * the new domain model and entity structure.
 */

@Repository
internal class IssueCommitConnectionDao
    @Autowired
    constructor(
        private val repository: IssueCommitConnectionRepository,
        private val issueRepository: IssueRepository,
        private val commitRepository: CommitRepository,
        private val issueMapper: IssueMapper,
    ) : IIssueCommitConnectionDao {
        @Autowired private lateinit var commitMapper: CommitMapper

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
         * Save an issue-commit connection.
         *
         * Uses the domain objects from [connection] directly on return to avoid requiring
         * an active MappingSession for the mapper round-trip.
         */
        override fun save(connection: IssueCommitConnection): IssueCommitConnection {
            val issueEntity =
                issueRepository.findById(connection.from.id!!).orElseThrow {
                    IllegalArgumentException("Issue with ID ${connection.from.id} not found")
                }
            val commitEntity =
                commitRepository.findById(connection.to.id!!).orElseThrow {
                    IllegalArgumentException("Commit with ID ${connection.to.id} not found")
                }

            val entity =
                IssueCommitConnectionEntity(
                    id = connection.id,
                    from = issueEntity,
                    to = commitEntity,
                )

            val savedEntity = repository.save(entity)

            return IssueCommitConnection(
                id = savedEntity.id,
                from = connection.from,
                to = connection.to,
            )
        }

        /**
         * Delete all issue-commit connections
         */
        override fun deleteAll() {
            repository.deleteAll()
        }
    }
