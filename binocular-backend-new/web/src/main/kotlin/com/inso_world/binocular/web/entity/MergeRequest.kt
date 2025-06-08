package com.inso_world.binocular.web.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.web.entity.edge.MergeRequestAccountConnection
import com.inso_world.binocular.web.entity.edge.MergeRequestMilestoneConnection
import com.inso_world.binocular.web.entity.edge.MergeRequestNoteConnection
import org.springframework.data.annotation.Id
import java.util.*

@Document("mergeRequests")
data class MergeRequest(
  @Id
  var id: String? = null,

  var iid: Int? = null,
  var title: String? = null,
  var description: String? = null,
  var createdAt: String? = null,
  var closedAt: String? = null,
  var updatedAt: String? = null,
  var labels: List<String> = emptyList(),
  var state: String? = null,
  var webUrl: String? = null,
  var mentions: List<Mention> = emptyList(),

  @Relations(
    edges = [MergeRequestAccountConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  var accounts: List<Account>? = null,

  @Relations(
    edges = [MergeRequestMilestoneConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  var milestones: List<Milestone>? = null,

  @Relations(
    edges = [MergeRequestNoteConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  var notes: List<Note>? = null
)
