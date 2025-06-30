package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific entity for a connection between an Issue and a User.
 */
@Entity
@Table(name = "issue_user_connections")
data class IssueUserConnectionEntity(
  @Id
  var id: String? = null,
  
  @Column(name = "issue_id")
  var issueId: String,
  
  @Column(name = "user_id")
  var userId: String
  
  // Note: Relationships to actual Issue and User entities would be handled
  // through JPA mappings or through separate queries in the DAO layer
)
