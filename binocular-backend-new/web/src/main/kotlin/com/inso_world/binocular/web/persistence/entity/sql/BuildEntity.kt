package com.inso_world.binocular.web.persistence.entity.sql

import com.inso_world.binocular.web.entity.Build
import jakarta.persistence.*
import java.util.*

/**
 * SQL-specific Build entity.
 */
@Entity
@Table(name = "builds")
data class BuildEntity(
  @Id
  var id: String? = null,
  
  var sha: String? = null,
  var ref: String? = null,
  var status: String? = null,
  var tag: String? = null,
  var user: String? = null,
  
  @Column(name = "user_full_name")
  var userFullName: String? = null,
  
  @Column(name = "created_at")
  @Temporal(TemporalType.TIMESTAMP)
  var createdAt: Date? = null,
  
  @Column(name = "updated_at")
  @Temporal(TemporalType.TIMESTAMP)
  var updatedAt: Date? = null,
  
  @Column(name = "started_at")
  @Temporal(TemporalType.TIMESTAMP)
  var startedAt: Date? = null,
  
  @Column(name = "finished_at")
  @Temporal(TemporalType.TIMESTAMP)
  var finishedAt: Date? = null,
  
  @Column(name = "committed_at")
  @Temporal(TemporalType.TIMESTAMP)
  var committedAt: Date? = null,
  
  var duration: Int? = null,
  
  @Column(name = "web_url")
  var webUrl: String? = null,
  
  // Jobs are stored as JSON in the database
  @Column(columnDefinition = "TEXT")
  var jobsJson: String? = null
  
  // Note: Relationships will be handled through JPA mappings in related entities
  // or through separate queries in the DAO layer
)
