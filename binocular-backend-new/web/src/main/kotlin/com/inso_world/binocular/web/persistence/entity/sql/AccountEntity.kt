package com.inso_world.binocular.web.persistence.entity.sql

import com.inso_world.binocular.web.entity.Platform
import jakarta.persistence.*

/**
 * SQL-specific Account entity.
 */
@Entity
@Table(name = "accounts")
data class AccountEntity(
  @Id
  var id: String? = null,
  
  @Enumerated(EnumType.STRING)
  var platform: Platform? = null,
  
  var login: String? = null,
  var name: String? = null,
  
  @Column(name = "avatar_url")
  var avatarUrl: String? = null,
  
  var url: String? = null
  
  // Note: Relationships will be handled through JPA mappings in related entities
  // or through separate queries in the DAO layer
)
