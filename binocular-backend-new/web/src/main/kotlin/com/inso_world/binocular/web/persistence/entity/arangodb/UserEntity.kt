package com.inso_world.binocular.web.persistence.entity.arangodb

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.Issue
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific User entity.
 */
@Document("users")
data class UserEntity(
  @Id
  var id: String? = null,
  var gitSignature: String,

  @Relations(
    edges = [CommitUserConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  var commits: List<CommitEntity>? = null,

  @Relations(
    edges = [IssueUserConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  var issues: List<IssueEntity>? = null,

  @Relations(
    edges = [CommitFileUserConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  var files: List<FileEntity>? = null
)
