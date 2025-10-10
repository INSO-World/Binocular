package com.inso_world.binocular.model

import java.time.LocalDateTime

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
    var createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null,
    var startedAt: LocalDateTime? = null,
    var finishedAt: LocalDateTime? = null,
    var committedAt: LocalDateTime? = null,
    var duration: Int? = null,
    var jobs: List<Job> = emptyList(),
    var webUrl: String? = null,
    // Relationships
    var commits: List<Commit> = emptyList(),
)
