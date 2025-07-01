package com.inso_world.binocular.web.entity

import java.util.*

/**
 * Domain model for a MergeRequest, representing a merge/pull request in a Git repository.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class MergeRequest(
  var id: String? = null,
  var iid: Int? = null,
  var title: String? = null,
  var description: String? = null,
  var createdAt: String? = null,
  var closedAt: String? = null,
  var updatedAt: String? = null,
  var labels: List<String> = emptyList(),
  var state: String? = null,
  var webUrl: String? = null,
  var mentions: List<Mention> = emptyList(),

  // Relationships
  var accounts: List<Account>? = null,
  var milestones: List<Milestone>? = null,
  var notes: List<Note>? = null
)
