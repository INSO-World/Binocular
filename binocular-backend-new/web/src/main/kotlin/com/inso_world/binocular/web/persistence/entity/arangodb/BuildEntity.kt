package com.inso_world.binocular.web.persistence.entity.arangodb

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.web.entity.Build
import com.inso_world.binocular.web.entity.Commit
import org.springframework.data.annotation.Id
import java.util.*

/**
 * ArangoDB-specific Build entity.
 */
@Document("builds")
data class BuildEntity(
  @Id
  var id: String? = null,
  var sha: String? = null,
  var ref: String? = null,
  var status: String? = null,
  var tag: String? = null,
  var user: String? = null,
  var userFullName: String? = null,
  var createdAt: Date? = null,
  var updatedAt: Date? = null,
  var startedAt: Date? = null,
  var finishedAt: Date? = null,
  var committedAt: Date? = null,
  var duration: Int? = null,
  var jobs: List<Build.Job>? = null,
  var webUrl: String? = null,

  @Relations(
    edges = [CommitBuildConnectionEntity::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  var commits: List<CommitEntity>? = null
)
