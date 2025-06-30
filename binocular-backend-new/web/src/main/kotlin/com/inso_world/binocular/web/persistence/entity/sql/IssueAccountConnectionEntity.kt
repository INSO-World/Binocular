package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific entity for a connection between an Issue and an Account.
 */
@Entity
@Table(name = "issue_account_connections")
data class IssueAccountConnectionEntity(
  @Id
  var id: String? = null,
  
  @Column(name = "issue_id")
  var issueId: String,
  
  @Column(name = "account_id")
  var accountId: String
  
  // Note: Relationships to actual Issue and Account entities would be handled
  // through JPA mappings or through separate queries in the DAO layer
)
