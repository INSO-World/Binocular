package com.inso_world.binocular.web.graphql.model

/**
 * Represents a single diff hunk in a file change within a commit.
 */
data class Hunk(
    val newStart: Int,
    val newLines: Int,
    val oldStart: Int,
    val oldLines: Int,
)
