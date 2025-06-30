package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific entity for a connection between a Note and an Account.
 */
@Entity
@Table(name = "note_account_connections")
data class NoteAccountConnectionEntity(
  @Id
  var id: String? = null,
  
  @Column(name = "note_id")
  var noteId: String,
  
  @Column(name = "account_id")
  var accountId: String
  
  // Note: Relationships to actual Note and Account entities would be handled
  // through JPA mappings or through separate queries in the DAO layer
)
