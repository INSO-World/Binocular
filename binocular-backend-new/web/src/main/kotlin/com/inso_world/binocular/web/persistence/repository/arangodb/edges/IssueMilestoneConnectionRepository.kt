package com.inso_world.binocular.web.persistence.repository.arangodb.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.persistence.entity.arangodb.IssueEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.IssueMilestoneConnectionEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.MilestoneEntity
import org.springframework.stereotype.Repository

@Repository
interface IssueMilestoneConnectionRepository: ArangoRepository<IssueMilestoneConnectionEntity, String> {

  @Query("""
    FOR c IN `issues-milestones`
        FILTER c._from == CONCAT('issues/', @issueId)
        FOR m IN milestones
            FILTER m._id == c._to
            RETURN m
""")
  fun findMilestonesByIssue(issueId: String): List<MilestoneEntity>

  @Query("""
    FOR c IN `issues-milestones`
        FILTER c._to == CONCAT('milestones/', @milestoneId)
        FOR i IN issues
            FILTER i._id == c._from
            RETURN i
""")
  fun findIssuesByMilestone(milestoneId: String): List<IssueEntity>
}
