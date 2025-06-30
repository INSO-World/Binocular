package com.inso_world.binocular.web.persistence.entity.arangodb

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.MergeRequest
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific Milestone entity.
 */
@Document("milestones")
data class MilestoneEntity(
  @Id
  var id: String? = null,
  var iid: Int? = null,
  var title: String? = null,
  var description: String? = null,
  var createdAt: String? = null,
  var updatedAt: String? = null,
  var startDate: String? = null,
  var dueDate: String? = null,
  var state: String? = null,
  var expired: Boolean? = null,
  var webUrl: String? = null,

  @Relations(
    edges = [IssueMilestoneConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  var issues: List<IssueEntity>? = null,

  @Relations(
    edges = [MergeRequestMilestoneConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  var mergeRequests: List<MergeRequestEntity>? = null
)
