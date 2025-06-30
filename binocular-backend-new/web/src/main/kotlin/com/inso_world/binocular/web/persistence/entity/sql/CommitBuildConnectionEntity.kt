package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific entity for a connection between a Commit and a Build.
 */
@Entity
@Table(name = "commit_build_connections")
data class CommitBuildConnectionEntity(
  @Id
  var id: String? = null,
  
  @Column(name = "commit_id")
  var commitId: String,
  
  @Column(name = "build_id")
  var buildId: String
  
  // Note: Relationships to actual Commit and Build entities would be handled
  // through JPA mappings or through separate queries in the DAO layer
)
