package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific entity for a connection between a Module and a File.
 */
@Entity
@Table(name = "module_file_connections")
data class ModuleFileConnectionEntity(
  @Id
  var id: String? = null,
  
  @Column(name = "module_id")
  var moduleId: String,
  
  @Column(name = "file_id")
  var fileId: String
  
  // Note: Relationships to actual Module and File entities would be handled
  // through JPA mappings or through separate queries in the DAO layer
)
