package com.inso_world.binocular.web.persistence.entity.sql.connections

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
) {
  // Default constructor for Hibernate
  constructor() : this(null, "", "")
}
