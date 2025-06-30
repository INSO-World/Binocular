package com.inso_world.binocular.web.entity

import java.util.*

/**
 * Domain model for a Build, representing a CI/CD build.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class Build(
  var id: String? = null,
  var sha: String? = null,
  var ref: String? = null,
  var status: String? = null,
  var tag: String? = null,
  var user: String? = null,
  var userFullName: String? = null,
  var createdAt: Date? = null,
  var updatedAt: Date? = null,
  var startedAt: Date? = null,
  var finishedAt: Date? = null,
  var committedAt: Date? = null,
  var duration: Int? = null,
  var jobs: List<Job>? = null,
  var webUrl: String? = null,

  // Relationships
  var commits: List<Commit>? = null
) {
  data class Job(
    var id: String? = null,
    var name: String? = null,
    var status: String? = null,
    var stage: String? = null,
    var createdAt: Date? = null,
    var finishedAt: Date? = null,
    var webUrl: String? = null
  )
}
