package com.inso_world.binocular.web.entity

/**
 * Domain model for a Note, representing a comment or note in a Git repository.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class Note(
  var id: String? = null,
  var body: String,
  var createdAt: String,
  var updatedAt: String,
  var system: Boolean = true,
  var resolvable: Boolean = false,
  var confidential: Boolean = false,
  var internal: Boolean = false,
  var imported: Boolean = false,
  var importedFrom: String,

  // Relationships
  var accounts: List<Account>? = null,
  var issues: List<Issue>? = null,
  var mergeRequests: List<MergeRequest>? = null
)
