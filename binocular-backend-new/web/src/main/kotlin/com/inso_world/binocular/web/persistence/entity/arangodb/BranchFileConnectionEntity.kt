package com.inso_world.binocular.web.persistence.entity.arangodb

import com.arangodb.springframework.annotation.Edge
import com.arangodb.springframework.annotation.From
import com.arangodb.springframework.annotation.To
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific entity for a connection between a Branch and a File.
 */
@Edge(value = "branches-files")
data class BranchFileConnectionEntity(
  @Id var id: String? = null,
  @From var from: BranchEntity,
  @To var to: FileEntity
)
