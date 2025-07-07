package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge

import com.inso_world.binocular.infrastructure.arangodb.model.edge.IssueMilestoneConnection
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Milestone

/**
 * Interface for IssueMilestoneConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
internal interface IIssueMilestoneConnectionDao {
    /**
     * Find all milestones connected to an issue
     */
    fun findMilestonesByIssue(issueId: String): List<Milestone>

    /**
     * Find all issues connected to a milestone
     */
    fun findIssuesByMilestone(milestoneId: String): List<Issue>

    /**
     * Save an issue-milestone connection
     */
    fun save(connection: IssueMilestoneConnection): IssueMilestoneConnection

    /**
     * Delete all issue-milestone connections
     */
    fun deleteAll()
}
