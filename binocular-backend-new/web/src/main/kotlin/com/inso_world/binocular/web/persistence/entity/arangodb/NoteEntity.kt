package com.inso_world.binocular.web.persistence.entity.arangodb

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.IssueNoteConnectionEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.MergeRequestNoteConnectionEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.NoteAccountConnectionEntity
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific Note entity.
 */
@Document("notes")
data class NoteEntity(
  @Id
  var id: String? = null,
  var body: String,
  var createdAt: String,
  var updatedAt: String,
  var system: Boolean = true,
  var resolvable: Boolean = false,
  var confidential: Boolean = false,
  var internal: Boolean = false,
  var imported: Boolean = false,
  var importedFrom: String,

  @Relations(
    edges = [NoteAccountConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  var accounts: List<AccountEntity>? = null,

  @Relations(
    edges = [IssueNoteConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  var issues: List<IssueEntity>? = null,

  @Relations(
    edges = [MergeRequestNoteConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  var mergeRequests: List<MergeRequestEntity>? = null
)
