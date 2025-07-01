package com.inso_world.binocular.web.entity.edge.domain

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.Issue

/**
 * Domain model for a connection between an Issue and a Commit.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class IssueCommitConnection(
  var id: String? = null,
  var from: Issue,
  var to: Commit
)
