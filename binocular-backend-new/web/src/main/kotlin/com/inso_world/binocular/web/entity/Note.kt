package com.inso_world.binocular.web.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.inso_world.binocular.web.entity.edge.IssueNoteConnection
import com.inso_world.binocular.web.entity.edge.MergeRequestNoteConnection
import com.inso_world.binocular.web.entity.edge.NoteAccountConnection
import org.springframework.data.annotation.Id

@Document("notes")
data class Note(
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
    edges = [NoteAccountConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  @JsonIgnoreProperties(value = ["notes"])
  var accounts: List<Account>? = null,

  @Relations(
    edges = [IssueNoteConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  @JsonIgnoreProperties(value = ["notes"])
  var issues: List<Issue>? = null,

  @Relations(
    edges = [MergeRequestNoteConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  @JsonIgnoreProperties(value = ["notes"])
  var mergeRequests: List<MergeRequest>? = null
)
