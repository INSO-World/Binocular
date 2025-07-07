package com.inso_world.binocular.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Size
import java.util.Date

/**
 * Domain model for a Commit, representing a commit in a Git repository.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class Commit(
    var id: String? = null,
    @field:NotBlank
    @field:Size(min = 40, max = 40)
    var sha: String? = null,
    @field:PastOrPresent
    @field:NotNull
    var date: Date? = null,
    @field:NotBlank
    var message: String? = null,
    var webUrl: String? = null,
    var branch: String? = null,
    var stats: Stats? = null,
    // Relationships
    var parents: List<Commit> = emptyList(),
    var children: List<Commit> = emptyList(),
    var builds: List<Build> = emptyList(),
    var files: List<File> = emptyList(),
    var modules: List<Module> = emptyList(),
    var users: List<User> = emptyList(),
    var issues: List<Issue> = emptyList(),
)

data class Stats(
    var additions: Long,
    var deletions: Long,
)
