package com.inso_world.binocular.web.entity

import java.util.*

/**
 * Domain model for an Issue, representing an issue in a Git repository.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class Issue(
  var id: String? = null,
  var iid: Int? = null,
  var title: String? = null,
  var description: String? = null,
  var createdAt: Date? = null,
  var closedAt: Date? = null,
  var updatedAt: Date? = null,
  var labels: List<String> = emptyList(),
  var state: String? = null,
  var webUrl: String? = null,
  var mentions: List<Mention> = emptyList(),

  // Relationships
  var accounts: List<Account>? = null,
  var commits: List<Commit>? = null,
  var milestones: List<Milestone>? = null,
  var notes: List<Note>? = null,
  var users: List<User>? = null
)

data class Mention(
  var commit: String? = null,
  var createdAt: Date? = null,
  var closes: Boolean? = null
)
