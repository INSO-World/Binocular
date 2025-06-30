package com.inso_world.binocular.web.persistence.entity.sql

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
  
  // Note: Relationships to actual MergeRequest and Account entities would be handled
  // through JPA mappings or through separate queries in the DAO layer
)
