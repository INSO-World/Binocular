package com.inso_world.binocular.web.persistence.entity.sql.connections

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
) {
  // Default constructor for Hibernate
  constructor() : this(null, "", "")
}
