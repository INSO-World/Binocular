package com.inso_world.binocular.web.entity

import com.arangodb.springframework.annotation.*
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.inso_world.binocular.web.entity.edge.*
import org.springframework.data.annotation.Id
import java.util.Date

@Document(collection = "commits")
data class Commit(
  @Id var id: String? = null,
  @Field("sha")
  @PersistentIndexed(unique = true)
  var sha: String? = null,
  var date: Date? = null,
  var message: String? = null,
  var webUrl: String? = null,
  var branch: String? = null,
  var stats: Stats? = null,

  @Relations(edges = [CommitCommitConnection::class], lazy = true, maxDepth = 1, direction = Relations.Direction.OUTBOUND)
  @JsonIgnoreProperties(value = ["parents", "children"])
  var parents: List<Commit>? = null,

  @Relations(edges = [CommitCommitConnection::class], lazy = true, maxDepth = 1, direction = Relations.Direction.INBOUND)
  @JsonIgnoreProperties(value = ["parents", "children"])
  var children: List<Commit>? = null,

  @Relations(edges = [CommitBuildConnection::class], lazy = true, maxDepth = 1, direction = Relations.Direction.OUTBOUND)
  @JsonIgnoreProperties(value = ["commits"])
  var builds: List<Build>? = null,

  @Relations(edges = [CommitFileConnection::class], lazy = true, maxDepth = 1, direction = Relations.Direction.OUTBOUND)
  @JsonIgnoreProperties(value = ["commits"])
  var files: List<File>? = null,

  @Relations(edges = [CommitModuleConnection::class], lazy = true, maxDepth = 1, direction = Relations.Direction.OUTBOUND)
  @JsonIgnoreProperties(value = ["commits"])
  var modules: List<Module>? = null,

  @Relations(edges = [CommitUserConnection::class], lazy = true, maxDepth = 1, direction = Relations.Direction.OUTBOUND)
  @JsonIgnoreProperties(value = ["commits"])
  var users: List<User>? = null,

  @Relations(edges = [IssueCommitConnection::class], lazy = true, maxDepth = 1, direction = Relations.Direction.INBOUND)
  @JsonIgnoreProperties(value = ["commits"])
  var issues: List<Issue>? = null
)

data class Stats(
  var additions: Long,
  var deletions: Long,
)
