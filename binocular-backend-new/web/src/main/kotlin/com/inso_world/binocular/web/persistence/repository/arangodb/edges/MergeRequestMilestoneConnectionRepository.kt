package com.inso_world.binocular.web.persistence.repository.arangodb.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.entity.Milestone
import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.edge.MergeRequestMilestoneConnection
import org.springframework.stereotype.Repository

@Repository
interface MergeRequestMilestoneConnectionRepository: ArangoRepository<MergeRequestMilestoneConnection, String> {

  @Query("""
    FOR c IN `merge-requests-milestones`
        FILTER c._from == CONCAT('mergeRequests/', @mergeRequestId)
        FOR m IN milestones
            FILTER m._id == c._to
            RETURN m
""")
  fun findMilestonesByMergeRequest(mergeRequestId: String): List<Milestone>

  @Query("""
    FOR c IN `merge-requests-milestones`
        FILTER c._to == CONCAT('milestones/', @milestoneId)
        FOR mr IN mergeRequests
            FILTER mr._id == c._from
            RETURN mr
""")
  fun findMergeRequestsByMilestone(milestoneId: String): List<MergeRequest>
}
