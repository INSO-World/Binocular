package com.inso_world.binocular.web.persistence.entity.sql

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.inso_world.binocular.web.entity.Mention
import jakarta.persistence.*
import java.util.*

/**
 * SQL-specific Issue entity.
 */
@Entity
@Table(name = "issues")
data class IssueEntity(
  @Id
  var id: String? = null,
  
  var iid: Int? = null,
  
  var title: String? = null,
  
  @Column(columnDefinition = "TEXT")
  var description: String? = null,
  
  @Column(name = "created_at")
  @Temporal(TemporalType.TIMESTAMP)
  var createdAt: Date? = null,
  
  @Column(name = "closed_at")
  @Temporal(TemporalType.TIMESTAMP)
  var closedAt: Date? = null,
  
  @Column(name = "updated_at")
  @Temporal(TemporalType.TIMESTAMP)
  var updatedAt: Date? = null,
  
  @Column(columnDefinition = "TEXT")
  var labelsJson: String? = null,
  
  var state: String? = null,
  
  @Column(name = "web_url")
  var webUrl: String? = null,
  
  @Column(columnDefinition = "TEXT")
  var mentionsJson: String? = null
  
  // Note: Relationships will be handled through JPA mappings in related entities
  // or through separate queries in the DAO layer
) {
  /**
   * Converts the JSON string to a list of labels
   */
  fun getLabels(objectMapper: ObjectMapper): List<String> {
    return if (labelsJson != null) {
      objectMapper.readValue(labelsJson, object : TypeReference<List<String>>() {})
    } else {
      emptyList()
    }
  }
  
  /**
   * Sets the labels list by converting it to a JSON string
   */
  fun setLabels(labels: List<String>, objectMapper: ObjectMapper) {
    this.labelsJson = objectMapper.writeValueAsString(labels)
  }
  
  /**
   * Converts the JSON string to a list of mentions
   */
  fun getMentions(objectMapper: ObjectMapper): List<Mention> {
    return if (mentionsJson != null) {
      objectMapper.readValue(mentionsJson, object : TypeReference<List<Mention>>() {})
    } else {
      emptyList()
    }
  }
  
  /**
   * Sets the mentions list by converting it to a JSON string
   */
  fun setMentions(mentions: List<Mention>, objectMapper: ObjectMapper) {
    this.mentionsJson = objectMapper.writeValueAsString(mentions)
  }
}
