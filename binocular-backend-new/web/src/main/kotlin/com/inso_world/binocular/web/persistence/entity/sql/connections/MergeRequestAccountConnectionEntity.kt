package com.inso_world.binocular.web.persistence.entity.sql.connections

import jakarta.persistence.*

/**
 * SQL-specific entity for a connection between a MergeRequest and an Account.
 */
@Entity
@Table(name = "merge_request_account_connections")
data class MergeRequestAccountConnectionEntity(
  @Id
  var id: String? = null,

  @Column(name = "merge_request_id")
  var mergeRequestId: String,

  @Column(name = "account_id")
  var accountId: String
) {
  // Default constructor for Hibernate
  constructor() : this(null, "", "")
}
