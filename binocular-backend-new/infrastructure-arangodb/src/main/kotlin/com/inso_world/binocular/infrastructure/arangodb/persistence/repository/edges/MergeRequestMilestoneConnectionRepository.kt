package com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.MergeRequestEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.MilestoneEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.MergeRequestMilestoneConnectionEntity
import org.springframework.stereotype.Repository

@Repository
interface MergeRequestMilestoneConnectionRepository : ArangoRepository<MergeRequestMilestoneConnectionEntity, String> {
    @Query(
        """
    FOR c IN `merge-requests-milestones`
        FILTER c._from == CONCAT('mergeRequests/', @mergeRequestId)
        FOR m IN milestones
            FILTER m._id == c._to
            RETURN m
""",
    )
    fun findMilestonesByMergeRequest(mergeRequestId: String): List<MilestoneEntity>

    @Query(
        """
    FOR c IN `merge-requests-milestones`
        FILTER c._to == CONCAT('milestones/', @milestoneId)
        FOR mr IN mergeRequests
            FILTER mr._id == c._from
            RETURN mr
""",
    )
    fun findMergeRequestsByMilestone(milestoneId: String): List<MergeRequestEntity>
}
