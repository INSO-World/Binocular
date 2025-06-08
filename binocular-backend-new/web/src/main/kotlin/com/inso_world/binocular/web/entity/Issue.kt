package com.inso_world.binocular.web.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Field
import com.arangodb.springframework.annotation.Relations
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

  var createdAt: Date? = null,

  var closedAt: Date? = null,

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
  var accounts: List<Account>? = null,

  @Relations(
    edges = [IssueCommitConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  var commits: List<Commit>? = null,

  @Relations(
    edges = [IssueMilestoneConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  var milestones: List<Milestone>? = null,

  @Relations(
    edges = [IssueNoteConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  var notes: List<Note>? = null,

  @Relations(
    edges = [IssueUserConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  var users: List<User>? = null
)

data class Mention(
  var commit: String? = null,
  var createdAt: Date? = null,
  var closes: Boolean? = null
)
