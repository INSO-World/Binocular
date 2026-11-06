package com.inso_world.binocular.model

import java.time.LocalDateTime

/**
 * Domain model for a Mention, representing a mention in a Git repository.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class Mention(
    var commit: String? = null,
    var createdAt: LocalDateTime? = null,
    var closes: Boolean? = null,
)
