package com.inso_world.binocular.web.entity.edge

import com.arangodb.springframework.annotation.Edge
import com.arangodb.springframework.annotation.From
import com.arangodb.springframework.annotation.To
import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Milestone
import org.springframework.data.annotation.Id

@Edge(value = "merge-requests-milestones")
data class MergeRequestMilestoneConnection(
  @Id var id: String? = null,
  @From var from: MergeRequest,
  @To var to: Milestone
)
