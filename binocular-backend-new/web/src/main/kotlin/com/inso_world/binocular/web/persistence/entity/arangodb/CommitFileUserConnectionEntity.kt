package com.inso_world.binocular.web.persistence.entity.arangodb

import com.arangodb.springframework.annotation.Edge
import com.arangodb.springframework.annotation.From
import com.arangodb.springframework.annotation.To
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific entity for a connection between a File and a User.
 */
@Edge(value = "commit-files-users")
data class CommitFileUserConnectionEntity(
  @Id var id: String? = null,
  @From var from: FileEntity,
  @To var to: UserEntity
)
