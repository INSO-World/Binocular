package com.inso_world.binocular.web.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.web.entity.edge.IssueMilestoneConnection
import com.inso_world.binocular.web.entity.edge.MergeRequestMilestoneConnection
import org.springframework.data.annotation.Id

@Document("milestones")
data class Milestone(
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
    edges = [IssueMilestoneConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  var issues: List<Issue>? = null,

  @Relations(
    edges = [MergeRequestMilestoneConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  var mergeRequests: List<MergeRequest>? = null
)
