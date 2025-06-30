package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific Note entity.
 */
@Entity
@Table(name = "notes")
data class NoteEntity(
  @Id
  var id: String? = null,
  
  @Column(columnDefinition = "TEXT")
  var body: String,
  
  @Column(name = "created_at")
  var createdAt: String,
  
  @Column(name = "updated_at")
  var updatedAt: String,
  
  var system: Boolean = true,
  var resolvable: Boolean = false,
  var confidential: Boolean = false,
  var internal: Boolean = false,
  var imported: Boolean = false,
  
  @Column(name = "imported_from")
  var importedFrom: String
  
  // Note: Relationships will be handled through JPA mappings in related entities
  // or through separate queries in the DAO layer
)
