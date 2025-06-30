package com.inso_world.binocular.web.entity

/**
 * Domain model for an Account, representing a user account from a platform like GitHub or GitLab.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class Account(
  var id: String? = null,
  var platform: Platform? = null,
  var login: String? = null,
  var name: String? = null,
  var avatarUrl: String? = null,
  var url: String? = null,

  // Relationships
  var issues: List<Issue>? = null,
  var mergeRequests: List<MergeRequest>? = null,
  var notes: List<Note>? = null
)

enum class Platform {
  GitHub,
  GitLab
}
