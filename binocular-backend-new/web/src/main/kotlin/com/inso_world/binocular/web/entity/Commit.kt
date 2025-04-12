package com.inso_world.binocular.web.entity

import com.arangodb.springframework.annotation.*
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
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
  @Relations(edges = [CommitParent::class], lazy = true, maxDepth = 1, direction = Relations.Direction.OUTBOUND)
  @JsonIgnoreProperties(value = ["parents", "children"])
  var parents: List<Commit>? = null,

  @Relations(edges = [CommitParent::class], lazy = true, maxDepth = 1, direction = Relations.Direction.INBOUND)
  @JsonIgnoreProperties(value = ["parents", "children"])
  var children: List<Commit>? = null
)


@Edge(value = "`commits-commits`")
data class CommitParent(
  @Id var id: String? = null,
)

data class Stats(
  var additions: Long,
  var deletions: Long,
)
