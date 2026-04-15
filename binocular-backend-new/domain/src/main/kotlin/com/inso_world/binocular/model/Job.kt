package com.inso_world.binocular.model

import java.time.LocalDateTime

/**
 * Domain model for a Job, representing the execution of a CI/CD job on a platform like GitHub or GitLab.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class Job(
    var id: String? = null,
    var name: String? = null,
    var status: String? = null,
    var stage: String? = null,
    var createdAt: LocalDateTime? = null,
    var finishedAt: LocalDateTime? = null,
    var webUrl: String? = null,
)
