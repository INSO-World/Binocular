package com.inso_world.binocular.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

/**
 * Domain model for a User, representing a Git user.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class User(
    var id: String? = null,
    @field:NotNull
    @field:NotBlank
    var gitSignature: String,
    // Relationships
    var commits: List<Commit> = emptyList(),
    var issues: List<Issue> = emptyList(),
    var files: List<File> = emptyList(),
)
