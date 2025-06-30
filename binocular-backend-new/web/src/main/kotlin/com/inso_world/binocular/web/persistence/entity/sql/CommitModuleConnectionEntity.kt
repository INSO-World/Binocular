package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific entity for a connection between a Commit and a Module.
 */
@Entity
@Table(name = "commit_module_connections")
data class CommitModuleConnectionEntity(
  @Id
  var id: String? = null,
  
  @Column(name = "commit_id")
  var commitId: String,
  
  @Column(name = "module_id")
  var moduleId: String
  
  // Note: Relationships to actual Commit and Module entities would be handled
  // through JPA mappings or through separate queries in the DAO layer
)
