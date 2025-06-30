package com.inso_world.binocular.web.entity

/**
 * Domain model for a Module, representing a code module or package in the codebase.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class Module(
  var id: String? = null,
  var path: String,

  // Relationships
  var commits: List<Commit>? = null,
  var files: List<File>? = null,
  var childModules: List<Module>? = null,
  var parentModules: List<Module>? = null
)
