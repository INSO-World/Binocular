package com.inso_world.binocular.web.persistence.entity.sql

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
  
  // Note: Relationships to actual MergeRequest and Note entities would be handled
  // through JPA mappings or through separate queries in the DAO layer
)
