package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific Module entity.
 */
@Entity
@Table(name = "modules")
data class ModuleEntity(
  @Id
  var id: String? = null,
  
  var path: String
  
  // Note: Relationships will be handled through JPA mappings in related entities
  // or through separate queries in the DAO layer
)
