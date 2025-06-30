package com.inso_world.binocular.web.persistence.entity.arangodb

import com.arangodb.springframework.annotation.Edge
import com.arangodb.springframework.annotation.From
import com.arangodb.springframework.annotation.To
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific entity for a connection between a MergeRequest and a Note.
 */
@Edge(value = "merge-requests-notes")
data class MergeRequestNoteConnectionEntity(
  @Id var id: String? = null,
  @From var from: MergeRequestEntity,
  @To var to: NoteEntity
)
