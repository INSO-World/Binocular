package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific entity for a connection between a MergeRequest and a Milestone.
 */
@Entity
@Table(name = "merge_request_milestone_connections")
data class MergeRequestMilestoneConnectionEntity(
  @Id
  var id: String? = null,
  
  @Column(name = "merge_request_id")
  var mergeRequestId: String,
  
  @Column(name = "milestone_id")
  var milestoneId: String
  
  // Note: Relationships to actual MergeRequest and Milestone entities would be handled
  // through JPA mappings or through separate queries in the DAO layer
)
