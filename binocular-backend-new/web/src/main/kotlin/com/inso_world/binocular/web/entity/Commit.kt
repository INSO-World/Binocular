package com.inso_world.binocular.web.entity

import java.util.Date

/**
 * Domain model for a Commit, representing a commit in a Git repository.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class Commit(
  var id: String? = null,
  var sha: String? = null,
  var date: Date? = null,
  var message: String? = null,
  var webUrl: String? = null,
  var branch: String? = null,
  var stats: Stats? = null,

  // Relationships
  var parents: List<Commit>? = null,
  var children: List<Commit>? = null,
  var builds: List<Build>? = null,
  var files: List<File>? = null,
  var modules: List<Module>? = null,
  var users: List<User>? = null,
  var issues: List<Issue>? = null
)

data class Stats(
  var additions: Long,
  var deletions: Long,
)
