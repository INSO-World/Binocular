package com.inso_world.binocular.web.persistence.entity.arangodb

import com.arangodb.springframework.annotation.Edge
import com.arangodb.springframework.annotation.From
import com.arangodb.springframework.annotation.To
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific entity for a connection between a Commit and a File.
 */
@Edge(value = "commits-files")
data class CommitFileConnectionEntity(
  @Id var id: String? = null,
  @From var from: CommitEntity,
  @To var to: FileEntity,
  var lineCount: Int? = null
)
