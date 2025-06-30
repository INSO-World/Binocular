package com.inso_world.binocular.web.entity

/**
 * Domain model for a Milestone, representing a milestone in a Git repository.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class Milestone(
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

  // Relationships
  var issues: List<Issue>? = null,
  var mergeRequests: List<MergeRequest>? = null
)
