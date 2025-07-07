package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge

import com.inso_world.binocular.infrastructure.arangodb.model.edge.MergeRequestMilestoneConnection
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Milestone

/**
 * Interface for MergeRequestMilestoneConnection DAO operations.
 * This interface is implemented by both ArangoDB and SQL DAOs.
 */
internal interface IMergeRequestMilestoneConnectionDao {
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
