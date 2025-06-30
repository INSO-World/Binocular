package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific File entity.
 */
@Entity
@Table(name = "files")
data class FileEntity(
  @Id
  var id: String? = null,
  
  var path: String,
  
  @Column(name = "web_url")
  var webUrl: String,
  
  @Column(name = "max_length")
  var maxLength: Int? = null
  
  // Note: Relationships will be handled through JPA mappings in related entities
  // or through separate queries in the DAO layer
)
