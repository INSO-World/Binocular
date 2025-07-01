package com.inso_world.binocular.web.persistence.entity.sql.connections

import jakarta.persistence.*

/**
 * SQL-specific entity for a connection between a MergeRequest and a Note.
 */
@Entity
@Table(name = "merge_request_note_connections")
data class MergeRequestNoteConnectionEntity(
  @Id
  var id: String? = null,

  @Column(name = "merge_request_id")
  var mergeRequestId: String,

  @Column(name = "note_id")
  var noteId: String
) {
  // Default constructor for Hibernate
  constructor() : this(null, "", "")
}
