package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific entity for a connection between a Commit and a User.
 */
@Entity
@Table(name = "commit_user_connections")
data class CommitUserConnectionEntity(
  @Id
  var id: String? = null,
  
  @Column(name = "commit_id")
  var commitId: String,
  
  @Column(name = "user_id")
  var userId: String
  
  // Note: Relationships to actual Commit and User entities would be handled
  // through JPA mappings or through separate queries in the DAO layer
)
