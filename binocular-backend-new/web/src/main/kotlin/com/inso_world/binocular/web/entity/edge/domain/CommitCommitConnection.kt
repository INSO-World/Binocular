package com.inso_world.binocular.web.entity.edge.domain

import com.inso_world.binocular.web.entity.Commit

/**
 * Domain model for a connection between two Commits.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class CommitCommitConnection(
  var id: String? = null,
  var from: Commit,
  var to: Commit
)
