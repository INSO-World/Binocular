package com.inso_world.binocular.web.persistence.entity.arangodb

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.entity.User
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific File entity.
 */
@Document("files")
data class FileEntity(
  @Id
  var id: String? = null,
  var path: String,
  var webUrl: String,
  var maxLength: Int? = null,

  @Relations(
    edges = [CommitFileConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  var commits: List<CommitEntity>? = null,

  @Relations(
    edges = [BranchFileConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  var branches: List<BranchEntity>? = null,

  @Relations(
    edges = [ModuleFileConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  var modules: List<ModuleEntity>? = null,

  @Relations(
    edges = [BranchFileFileConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  var relatedFiles: List<FileEntity>? = null,

  @Relations(
    edges = [CommitFileUserConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  var users: List<UserEntity>? = null
)
