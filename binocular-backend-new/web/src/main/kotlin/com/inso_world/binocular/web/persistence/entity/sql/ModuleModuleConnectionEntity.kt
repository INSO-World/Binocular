package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific entity for a connection between two Modules.
 */
@Entity
@Table(name = "module_module_connections")
data class ModuleModuleConnectionEntity(
  @Id
  var id: String? = null,
  
  @Column(name = "from_module_id")
  var fromModuleId: String,
  
  @Column(name = "to_module_id")
  var toModuleId: String
  
  // Note: Relationships to actual Module entities would be handled
  // through JPA mappings or through separate queries in the DAO layer
)
