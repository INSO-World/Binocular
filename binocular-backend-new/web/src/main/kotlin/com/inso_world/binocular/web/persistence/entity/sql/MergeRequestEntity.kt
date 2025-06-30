package com.inso_world.binocular.web.persistence.entity.sql

import com.inso_world.binocular.web.entity.Mention
import jakarta.persistence.*
import java.util.*

/**
 * SQL-specific MergeRequest entity.
 */
@Entity
@Table(name = "merge_requests")
data class MergeRequestEntity(
  @Id
  var id: String? = null,
  
  var iid: Int? = null,
  var title: String? = null,
  
  @Column(columnDefinition = "TEXT")
  var description: String? = null,
  
  @Column(name = "created_at")
  var createdAt: String? = null,
  
  @Column(name = "closed_at")
  var closedAt: String? = null,
  
  @Column(name = "updated_at")
  var updatedAt: String? = null,
  
  @ElementCollection
  @CollectionTable(name = "merge_request_labels", joinColumns = [JoinColumn(name = "merge_request_id")])
  @Column(name = "label")
  var labels: List<String> = emptyList(),
  
  var state: String? = null,
  
  @Column(name = "web_url")
  var webUrl: String? = null,
  
  // Note: Mentions will be stored as JSON in the database
  // This would typically be handled with a custom converter
  // For simplicity, we're not implementing the full converter here
  @Column(columnDefinition = "TEXT")
  var mentionsJson: String? = null
  
  // Note: Relationships will be handled through JPA mappings in related entities
  // or through separate queries in the DAO layer
)
