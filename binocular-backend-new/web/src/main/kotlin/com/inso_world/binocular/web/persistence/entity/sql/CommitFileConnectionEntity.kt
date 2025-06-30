package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific entity for a connection between a Commit and a File.
 */
@Entity
@Table(name = "commit_file_connections")
data class CommitFileConnectionEntity(
  @Id
  var id: String? = null,
  
  @Column(name = "commit_id")
  var commitId: String,
  
  @Column(name = "file_id")
  var fileId: String,
  
  @Column(name = "line_count")
  var lineCount: Int? = null
  
  // Note: Relationships to actual Commit and File entities would be handled
  // through JPA mappings or through separate queries in the DAO layer
)
