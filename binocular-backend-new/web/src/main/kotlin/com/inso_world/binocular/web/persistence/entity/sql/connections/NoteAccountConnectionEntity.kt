package com.inso_world.binocular.web.persistence.entity.sql.connections

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
) {
  // Default constructor for Hibernate
  constructor() : this(null, "", "")
}
