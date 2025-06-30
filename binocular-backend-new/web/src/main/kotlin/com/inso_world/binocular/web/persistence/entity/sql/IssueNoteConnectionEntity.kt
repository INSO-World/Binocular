package com.inso_world.binocular.web.persistence.entity.sql

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
  
  // Note: Relationships to actual Issue and Note entities would be handled
  // through JPA mappings or through separate queries in the DAO layer
)
