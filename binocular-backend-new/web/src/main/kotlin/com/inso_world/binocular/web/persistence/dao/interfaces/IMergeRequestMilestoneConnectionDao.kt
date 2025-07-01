package com.inso_world.binocular.web.persistence.dao.interfaces

import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Milestone
import com.inso_world.binocular.web.entity.edge.domain.MergeRequestMilestoneConnection

/**
 * Interface for MergeRequestMilestoneConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
interface IMergeRequestMilestoneConnectionDao {
    
    /**
     * Find all milestones connected to a merge request
     */
    fun findMilestonesByMergeRequest(mergeRequestId: String): List<Milestone>
    
    /**
     * Find all merge requests connected to a milestone
     */
    fun findMergeRequestsByMilestone(milestoneId: String): List<MergeRequest>
    
    /**
     * Save a merge request-milestone connection
     */
    fun save(connection: MergeRequestMilestoneConnection): MergeRequestMilestoneConnection
    
    /**
     * Delete all merge request-milestone connections
     */
    fun deleteAll()
}
