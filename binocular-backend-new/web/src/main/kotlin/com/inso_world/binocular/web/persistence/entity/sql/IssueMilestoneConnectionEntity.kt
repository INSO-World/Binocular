package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific entity for a connection between an Issue and a Milestone.
 */
@Entity
@Table(name = "issue_milestone_connections")
data class IssueMilestoneConnectionEntity(
  @Id
  var id: String? = null,
  
  @Column(name = "issue_id")
  var issueId: String,
  
  @Column(name = "milestone_id")
  var milestoneId: String
  
  // Note: Relationships to actual Issue and Milestone entities would be handled
  // through JPA mappings or through separate queries in the DAO layer
)
