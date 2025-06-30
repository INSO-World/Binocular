package com.inso_world.binocular.web.persistence.entity.sql.connections

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
) {
  // Default constructor for Hibernate
  constructor() : this(null, "", "")
}
