package com.inso_world.binocular.web.persistence.entity.arangodb

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.File
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific Module entity.
 */
@Document("modules")
data class ModuleEntity(
  @Id
  var id: String? = null,
  var path: String,

  @Relations(
    edges = [CommitModuleConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  var commits: List<CommitEntity>? = null,

  @Relations(
    edges = [ModuleFileConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  var files: List<FileEntity>? = null,

  @Relations(
    edges = [ModuleModuleConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  var childModules: List<ModuleEntity>? = null,

  @Relations(
    edges = [ModuleModuleConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  var parentModules: List<ModuleEntity>? = null
)
