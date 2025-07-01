package com.inso_world.binocular.web.persistence.dao.interfaces

import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.Milestone
import com.inso_world.binocular.web.entity.edge.domain.IssueMilestoneConnection

/**
 * Interface for IssueMilestoneConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
interface IIssueMilestoneConnectionDao {
    
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
