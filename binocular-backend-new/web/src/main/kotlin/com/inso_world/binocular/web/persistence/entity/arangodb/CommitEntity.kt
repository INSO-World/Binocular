package com.inso_world.binocular.web.persistence.entity.arangodb

import com.arangodb.springframework.annotation.*
import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.CommitBuildConnectionEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.CommitCommitConnectionEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.CommitFileConnectionEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.CommitModuleConnectionEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.CommitUserConnectionEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.IssueCommitConnectionEntity
import org.springframework.data.annotation.Id
import java.util.Date

/**
 * ArangoDB-specific Commit entity.
 */
@Document(collection = "commits")
data class CommitEntity(
  @Id var id: String? = null,

  @Field("sha")
  @PersistentIndexed(unique = true)
  var sha: String? = null,

  var date: Date? = null,
  var message: String? = null,
  var webUrl: String? = null,
  var branch: String? = null,
  var stats: Stats? = null,

  @Relations(edges = [CommitCommitConnectionEntity::class], lazy = true, maxDepth = 1, direction = Relations.Direction.OUTBOUND)
  var parents: List<CommitEntity>? = null,

  @Relations(edges = [CommitCommitConnectionEntity::class], lazy = true, maxDepth = 1, direction = Relations.Direction.INBOUND)
  var children: List<CommitEntity>? = null,

  @Relations(edges = [CommitBuildConnectionEntity::class], lazy = true, maxDepth = 1, direction = Relations.Direction.OUTBOUND)
  var builds: List<BuildEntity>? = null,

  @Relations(edges = [CommitFileConnectionEntity::class], lazy = true, maxDepth = 1, direction = Relations.Direction.OUTBOUND)
  var files: List<FileEntity>? = null,

  @Relations(edges = [CommitModuleConnectionEntity::class], lazy = true, maxDepth = 1, direction = Relations.Direction.OUTBOUND)
  var modules: List<ModuleEntity>? = null,

  @Relations(edges = [CommitUserConnectionEntity::class], lazy = true, maxDepth = 1, direction = Relations.Direction.OUTBOUND)
  var users: List<UserEntity>? = null,

  @Relations(edges = [IssueCommitConnectionEntity::class], lazy = true, maxDepth = 1, direction = Relations.Direction.INBOUND)
  var issues: List<IssueEntity>? = null
)
