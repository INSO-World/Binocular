package com.inso_world.binocular.web.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.inso_world.binocular.web.entity.edge.CommitUserConnection
import com.inso_world.binocular.web.entity.edge.IssueUserConnection
import org.springframework.data.annotation.Id

@Document("users")
data class User(
  @Id
  var id: String? = null,
  var gitSignature: String,

  @Relations(
    edges = [CommitUserConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  @JsonIgnoreProperties(value = ["users"])
  var commits: List<Commit>? = null,

  @Relations(
    edges = [IssueUserConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  @JsonIgnoreProperties(value = ["users"])
  var issues: List<Issue>? = null
)
