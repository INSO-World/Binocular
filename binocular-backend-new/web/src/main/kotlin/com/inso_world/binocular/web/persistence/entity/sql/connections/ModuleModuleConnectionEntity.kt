package com.inso_world.binocular.web.persistence.entity.sql.connections

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
) {
  // Default constructor for Hibernate
  constructor() : this(null, "", "")
}
