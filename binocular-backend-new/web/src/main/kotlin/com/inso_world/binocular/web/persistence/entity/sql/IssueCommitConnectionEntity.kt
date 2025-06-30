package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific entity for a connection between an Issue and a Commit.
 */
@Entity
@Table(name = "issue_commit_connections")
data class IssueCommitConnectionEntity(
  @Id
  var id: String? = null,
  
  @Column(name = "issue_id")
  var issueId: String,
  
  @Column(name = "commit_id")
  var commitId: String
  
  // Note: Relationships to actual Issue and Commit entities would be handled
  // through JPA mappings or through separate queries in the DAO layer
)
