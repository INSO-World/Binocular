package com.inso_world.binocular.web.entity

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
  var commits: List<Commit>? = null,
  var branches: List<Branch>? = null,
  var modules: List<Module>? = null,
  var relatedFiles: List<File>? = null,
  var users: List<User>? = null
)
