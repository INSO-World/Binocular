package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.Milestone
import com.inso_world.binocular.web.entity.edge.domain.IssueMilestoneConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueMilestoneConnectionDao
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.IssueMilestoneConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.arangodb.IssueMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.MilestoneMapper
import com.inso_world.binocular.web.persistence.repository.arangodb.IssueRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.MilestoneRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueMilestoneConnectionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IIssueMilestoneConnectionDao.
 * 
 * This class adapts the existing IssueMilestoneConnectionRepository to work with
 * the new domain model and entity structure.
 */
@Repository
@Profile("nosql", "arangodb")
class IssueMilestoneConnectionDao @Autowired constructor(
    private val repository: IssueMilestoneConnectionRepository,
    private val issueRepository: IssueRepository,
    private val milestoneRepository: MilestoneRepository,
    private val issueMapper: IssueMapper,
    private val milestoneMapper: MilestoneMapper
) : IIssueMilestoneConnectionDao {

    /**
     * Find all milestones connected to an issue
     */
    override fun findMilestonesByIssue(issueId: String): List<Milestone> {
        val milestoneEntities = repository.findMilestonesByIssue(issueId)
        return milestoneEntities.map { milestoneMapper.toDomain(it) }

    }

    /**
     * Find all issues connected to a milestone
     */
    override fun findIssuesByMilestone(milestoneId: String): List<Issue> {
        val issueEntities = repository.findIssuesByMilestone(milestoneId)
        return issueEntities.map { issueMapper.toDomain(it) }
    }

    /**
     * Save an issue-milestone connection
     */
    override fun save(connection: IssueMilestoneConnection): IssueMilestoneConnection {
        // Get the issue and milestone entities from their repositories
        val issueEntity = issueRepository.findById(connection.from.id!!).orElseThrow { 
            IllegalArgumentException("Issue with ID ${connection.from.id} not found") 
        }
        val milestoneEntity = milestoneRepository.findById(connection.to.id!!).orElseThrow { 
            IllegalArgumentException("Milestone with ID ${connection.to.id} not found") 
        }

        // Convert domain model to the entity format
        val entity = IssueMilestoneConnectionEntity(
            id = connection.id,
            from = issueEntity,
            to = milestoneEntity
        )

        // Save using the repository
        val savedEntity = repository.save(entity)

        // Convert back to domain model
        return IssueMilestoneConnection(
            id = savedEntity.id,
            from = issueMapper.toDomain(savedEntity.from),
            to = milestoneMapper.toDomain(savedEntity.to)
        )
    }

    /**
     * Delete all issue-milestone connections
     */
    override fun deleteAll() {
        repository.deleteAll()
    }
}
