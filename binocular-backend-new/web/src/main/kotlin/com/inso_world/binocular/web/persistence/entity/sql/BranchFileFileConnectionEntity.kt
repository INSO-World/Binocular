package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific entity for a connection between a BranchFile and a File.
 */
@Entity
@Table(name = "branch_file_file_connections")
data class BranchFileFileConnectionEntity(
  @Id
  var id: String? = null,
  
  @Column(name = "branch_file_id")
  var branchFileId: String,
  
  @Column(name = "file_id")
  var fileId: String
  
  // Note: Relationships to actual File entities would be handled
  // through JPA mappings or through separate queries in the DAO layer
)
