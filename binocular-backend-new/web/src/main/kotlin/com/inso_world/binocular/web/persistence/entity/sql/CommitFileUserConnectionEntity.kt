package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific entity for a connection between a File and a User.
 */
@Entity
@Table(name = "commit_file_user_connections")
data class CommitFileUserConnectionEntity(
  @Id
  var id: String? = null,
  
  @Column(name = "file_id")
  var fileId: String,
  
  @Column(name = "user_id")
  var userId: String
  
  // Note: Relationships to actual File and User entities would be handled
  // through JPA mappings or through separate queries in the DAO layer
)
