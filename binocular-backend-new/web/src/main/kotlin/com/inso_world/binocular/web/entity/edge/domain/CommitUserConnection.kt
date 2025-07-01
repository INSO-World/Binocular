package com.inso_world.binocular.web.entity.edge.domain

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.User

/**
 * Domain model for a connection between a Commit and a User.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class CommitUserConnection(
  var id: String? = null,
  var from: Commit,
  var to: User
)
