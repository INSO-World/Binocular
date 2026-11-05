package com.inso_world.binocular.model

/**
 * Ownership data structures used for commit-file ownership exposure.
 */

data class OwnershipLine(
    val from: Int,
    val to: Int,
)

data class OwnershipHunk(
    val originalCommit: String?,
    val lines: List<OwnershipLine>,
)

data class FileOwnership(
    val user: String,
    val hunks: List<OwnershipHunk>,
)
