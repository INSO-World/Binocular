package com.inso_world.binocular.web.persistence.entity.sql.connections

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
) {
  // Default constructor for Hibernate
  constructor() : this(null, "", "")
}
