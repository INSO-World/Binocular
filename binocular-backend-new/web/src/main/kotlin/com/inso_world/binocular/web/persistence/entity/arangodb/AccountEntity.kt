package com.inso_world.binocular.web.persistence.entity.arangodb

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.web.entity.Platform
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.IssueAccountConnectionEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.MergeRequestAccountConnectionEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.NoteAccountConnectionEntity
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific Account entity.
 */
@Document("accounts")
data class AccountEntity(
  @Id
  var id: String? = null,
  var platform: Platform? = null,
  var login: String? = null,
  var name: String? = null,
  var avatarUrl: String? = null,
  var url: String? = null,

  @Relations(
    edges = [IssueAccountConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  var issues: List<IssueEntity>? = null,

  @Relations(
    edges = [MergeRequestAccountConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  var mergeRequests: List<MergeRequestEntity>? = null,

  @Relations(
    edges = [NoteAccountConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  var notes: List<NoteEntity>? = null
)
