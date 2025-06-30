package com.inso_world.binocular.web.entity.edge.domain

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.MergeRequest

/**
 * Domain model for a connection between a MergeRequest and an Account.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class MergeRequestAccountConnection(
  var id: String? = null,
  var from: MergeRequest,
  var to: Account
)
