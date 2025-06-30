package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific Branch entity.
 */
@Entity
@Table(name = "branches")
data class BranchEntity(
  @Id
  var id: String? = null,
  
  var branch: String? = null,
  
  var active: Boolean = false,
  
  @Column(name = "tracks_file_renames")
  var tracksFileRenames: Boolean = false,
  
  @Column(name = "latest_commit")
  var latestCommit: String? = null
  
  // Note: Relationships will be handled through JPA mappings in related entities
  // or through separate queries in the DAO layer
)
