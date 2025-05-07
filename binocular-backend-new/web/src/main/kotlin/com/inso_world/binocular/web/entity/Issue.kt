package com.inso_world.binocular.web.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Field
import com.arangodb.springframework.annotation.Relations
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.inso_world.binocular.web.entity.edge.IssueAccountConnection
import com.inso_world.binocular.web.entity.edge.IssueCommitConnection
import com.inso_world.binocular.web.entity.edge.IssueMilestoneConnection
import com.inso_world.binocular.web.entity.edge.IssueNoteConnection
import com.inso_world.binocular.web.entity.edge.IssueUserConnection
import org.springframework.data.annotation.Id
import java.util.*

@Document("issues")
data class Issue(
  @Id
  var id: String? = null,

  @Field("iid")
  var iid: Int? = null,

  var title: String? = null,
  var description: String? = null,

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
  var createdAt: Date? = null,

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
  var closedAt: Date? = null,

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
  var updatedAt: Date? = null,

  var labels: List<String> = emptyList(),
  var state: String? = null,
  var webUrl: String? = null,

  var mentions: List<Mention> = emptyList(),

  @Relations(
    edges = [IssueAccountConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  @JsonIgnoreProperties(value = ["issues"])
  var accounts: List<Account>? = null,

  @Relations(
    edges = [IssueCommitConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  @JsonIgnoreProperties(value = ["issues"])
  var commits: List<Commit>? = null,

  @Relations(
    edges = [IssueMilestoneConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  @JsonIgnoreProperties(value = ["issues"])
  var milestones: List<Milestone>? = null,

  @Relations(
    edges = [IssueNoteConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  @JsonIgnoreProperties(value = ["issues"])
  var notes: List<Note>? = null,

  @Relations(
    edges = [IssueUserConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  @JsonIgnoreProperties(value = ["issues"])
  var users: List<User>? = null
)

data class Mention(
  var commit: String? = null,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
  var createdAt: Date? = null,
  var closes: Boolean? = null
)
