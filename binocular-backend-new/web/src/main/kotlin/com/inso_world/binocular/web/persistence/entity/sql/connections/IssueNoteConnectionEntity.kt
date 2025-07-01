package com.inso_world.binocular.web.persistence.entity.sql.connections

import jakarta.persistence.*

/**
 * SQL-specific entity for a connection between an Issue and a Note.
 */
@Entity
@Table(name = "issue_note_connections")
data class IssueNoteConnectionEntity(
  @Id
  var id: String? = null,

  @Column(name = "issue_id")
  var issueId: String,

  @Column(name = "note_id")
  var noteId: String
) {
  // Default constructor for Hibernate
  constructor() : this(null, "", "")
}
