package com.inso_world.binocular.model


/**
 * Domain model for Stats, representing the addition and deletion statistics of a commit.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class Stats(
    var additions: Long,
    var deletions: Long,
)

