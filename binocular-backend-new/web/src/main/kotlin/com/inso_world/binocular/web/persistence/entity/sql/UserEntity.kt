package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific User entity.
 */
@Entity
@Table(name = "users")
data class UserEntity(
  @Id
  var id: String? = null,
  
  @Column(name = "git_signature")
  var gitSignature: String
  
  // Note: Relationships will be handled through JPA mappings in related entities
  // or through separate queries in the DAO layer
)
