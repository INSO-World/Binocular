package com.inso_world.binocular.model

/**
 * Domain model for a Branch, representing a branch in a Git repository.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class Branch(
    var id: String? = null,
    var branch: String? = null,
    var active: Boolean = false,
    var tracksFileRenames: Boolean = false,
    var latestCommit: String? = null,
    // Relationships
    var files: List<File> = emptyList(),
)
