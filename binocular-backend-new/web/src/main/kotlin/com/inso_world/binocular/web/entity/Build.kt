package com.inso_world.binocular.web.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.inso_world.binocular.web.entity.edge.CommitBuildConnection
import org.springframework.data.annotation.Id
import java.util.*

@Document("builds")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Build(
  @Id
  var id: String? = null,
  var sha: String? = null,
  var ref: String? = null,
  var status: String? = null,
  var tag: String? = null,
  var user: String? = null,
  var userFullName: String? = null,
  var createdAt: Date? = null,
  var updatedAt: Date? = null,
  var startedAt: Date? = null,
  var finishedAt: Date? = null,
  var committedAt: Date? = null,
  var duration: Int? = null,
  var jobs: List<Job>? = null,
  var webUrl: String? = null,

  @Relations(
    edges = [CommitBuildConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  @JsonIgnoreProperties(value = ["builds"])
  var commits: List<Commit>? = null
) {
  data class Job(
    var id: String? = null,
    var name: String? = null,
    var status: String? = null,
    var stage: String? = null,
    var createdAt: Date? = null,
    var finishedAt: Date? = null,
    var webUrl: String? = null
  )
}
