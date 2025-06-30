package com.inso_world.binocular.web.persistence.entity.arangodb

import com.arangodb.springframework.annotation.Edge
import com.arangodb.springframework.annotation.From
import com.arangodb.springframework.annotation.To
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific entity for a connection between an Issue and a Note.
 */
@Edge(value = "issues-notes")
data class IssueNoteConnectionEntity(
  @Id var id: String? = null,
  @From var from: IssueEntity,
  @To var to: NoteEntity
)
