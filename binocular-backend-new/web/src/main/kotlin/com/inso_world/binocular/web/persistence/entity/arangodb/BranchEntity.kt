package com.inso_world.binocular.web.persistence.entity.arangodb

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.web.entity.File
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific Branch entity.
 */
@Document("branches")
data class BranchEntity(
  @Id
  var id: String? = null,
  var branch: String? = null,
  var active: Boolean = false,
  var tracksFileRenames: Boolean = false,
  var latestCommit: String? = null,

  @Relations(
    edges = [BranchFileConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  var files: List<FileEntity>? = null
)
