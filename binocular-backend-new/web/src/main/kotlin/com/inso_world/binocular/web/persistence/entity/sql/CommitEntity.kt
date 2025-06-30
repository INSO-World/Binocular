package com.inso_world.binocular.web.persistence.entity.sql

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.inso_world.binocular.web.entity.Stats
import jakarta.persistence.*
import java.util.*

/**
 * SQL-specific Commit entity.
 */
@Entity
@Table(name = "commits")
data class CommitEntity(
  @Id
  var id: String? = null,
  
  @Column(unique = true)
  var sha: String? = null,
  
  @Temporal(TemporalType.TIMESTAMP)
  var date: Date? = null,
  
  @Column(columnDefinition = "TEXT")
  var message: String? = null,
  
  @Column(name = "web_url")
  var webUrl: String? = null,
  
  var branch: String? = null,
  
  @Column(columnDefinition = "TEXT")
  var statsJson: String? = null
  
  // Note: Relationships will be handled through JPA mappings in related entities
  // or through separate queries in the DAO layer
) {
  /**
   * Gets the stats object from the JSON representation.
   */
  fun getStats(objectMapper: ObjectMapper): Stats? {
    return if (statsJson != null) {
      objectMapper.readValue(statsJson, Stats::class.java)
    } else {
      null
    }
  }
  
  /**
   * Sets the stats object and converts it to a JSON representation.
   */
  fun setStats(stats: Stats?, objectMapper: ObjectMapper) {
    statsJson = if (stats != null) {
      objectMapper.writeValueAsString(stats)
    } else {
      null
    }
  }
}
