package com.inso_world.binocular.web.persistence.entity.arangodb

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.web.entity.Mention
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.MergeRequestAccountConnectionEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.MergeRequestMilestoneConnectionEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.MergeRequestNoteConnectionEntity
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific MergeRequest entity.
 */
@Document("mergeRequests")
data class MergeRequestEntity(
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
    edges = [MergeRequestAccountConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  var accounts: List<AccountEntity>? = null,

  @Relations(
    edges = [MergeRequestMilestoneConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  var milestones: List<MilestoneEntity>? = null,

  @Relations(
    edges = [MergeRequestNoteConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  var notes: List<NoteEntity>? = null
)
