package com.inso_world.binocular.web.entity.edge.domain

import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Milestone

/**
 * Domain model for a connection between a MergeRequest and a Milestone.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class MergeRequestMilestoneConnection(
  var id: String? = null,
  var from: MergeRequest,
  var to: Milestone
)
