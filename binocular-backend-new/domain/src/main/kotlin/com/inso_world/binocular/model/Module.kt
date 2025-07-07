package com.inso_world.binocular.model

/**
 * Domain model for a Module, representing a code module or package in the codebase.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class Module(
    var id: String? = null,
    var path: String,
    // Relationships
    var commits: List<Commit> = emptyList(),
    var files: List<File> = emptyList(),
    var childModules: List<Module> = emptyList(),
    var parentModules: List<Module> = emptyList(),
)
