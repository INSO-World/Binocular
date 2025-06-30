package com.inso_world.binocular.web.entity

/**
 * Domain model for a User, representing a Git user.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class User(
  var id: String? = null,
  var gitSignature: String,

  // Relationships
  var commits: List<Commit>? = null,
  var issues: List<Issue>? = null,
  var files: List<File>? = null
)
