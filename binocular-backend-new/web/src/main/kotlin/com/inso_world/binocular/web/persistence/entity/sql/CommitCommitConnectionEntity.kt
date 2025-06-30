package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific entity for a connection between two Commits.
 */
@Entity
@Table(name = "commit_commit_connections")
data class CommitCommitConnectionEntity(
  @Id
  var id: String? = null,
  
  @Column(name = "from_commit_id")
  var fromCommitId: String,
  
  @Column(name = "to_commit_id")
  var toCommitId: String
  
  // Note: Relationships to actual Commit entities would be handled
  // through JPA mappings or through separate queries in the DAO layer
)
