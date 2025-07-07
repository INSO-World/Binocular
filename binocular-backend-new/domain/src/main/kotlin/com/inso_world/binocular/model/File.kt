package com.inso_world.binocular.model

/**
 * Domain model for a File, representing a file in a Git repository.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class File(
    var id: String? = null,
    var path: String,
    var webUrl: String,
    var maxLength: Int? = null,
    // Relationships
    var commits: List<Commit> = emptyList(),
    var branches: List<Branch> = emptyList(),
    var modules: List<Module> = emptyList(),
    var relatedFiles: List<File> = emptyList(),
    var users: List<User> = emptyList(),
)
