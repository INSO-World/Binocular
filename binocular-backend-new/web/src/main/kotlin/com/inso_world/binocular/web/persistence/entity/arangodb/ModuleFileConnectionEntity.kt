package com.inso_world.binocular.web.persistence.entity.arangodb

import com.arangodb.springframework.annotation.Edge
import com.arangodb.springframework.annotation.From
import com.arangodb.springframework.annotation.To
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific entity for a connection between a Module and a File.
 */
@Edge(value = "modules-files")
data class ModuleFileConnectionEntity(
  @Id var id: String? = null,
  @From var from: ModuleEntity,
  @To var to: FileEntity
)
