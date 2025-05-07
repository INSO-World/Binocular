package com.inso_world.binocular.web.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.inso_world.binocular.web.entity.edge.IssueAccountConnection
import com.inso_world.binocular.web.entity.edge.MergeRequestAccountConnection
import com.inso_world.binocular.web.entity.edge.NoteAccountConnection
import org.springframework.data.annotation.Id

@Document("accounts")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Account(
  @Id
  var id: String? = null,
  var platform: Platform? = null,
  var login: String? = null,
  var name: String? = null,
  var avatarUrl: String? = null,
  var url: String? = null,

  @Relations(
    edges = [IssueAccountConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  @JsonIgnoreProperties(value = ["accounts"])
  var issues: List<Issue>? = null,

  @Relations(
    edges = [MergeRequestAccountConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  @JsonIgnoreProperties(value = ["accounts"])
  var mergeRequests: List<MergeRequest>? = null,

  @Relations(
    edges = [NoteAccountConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  @JsonIgnoreProperties(value = ["accounts"])
  var notes: List<Note>? = null
)

enum class Platform {
  GitHub,
  GitLab
}
