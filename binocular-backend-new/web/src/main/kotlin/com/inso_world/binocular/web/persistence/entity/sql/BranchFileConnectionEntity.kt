package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific entity for a connection between a Branch and a File.
 */
@Entity
@Table(name = "branch_file_connections")
data class BranchFileConnectionEntity(
  @Id
  var id: String? = null,
  
  @Column(name = "branch_id")
  var branchId: String,
  
  @Column(name = "file_id")
  var fileId: String
  
  // Note: Relationships to actual Branch and File entities would be handled
  // through JPA mappings or through separate queries in the DAO layer
)
