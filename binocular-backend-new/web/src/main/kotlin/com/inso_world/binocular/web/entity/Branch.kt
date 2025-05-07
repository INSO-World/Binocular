package com.inso_world.binocular.web.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.inso_world.binocular.web.entity.edge.BranchFileConnection
import org.springframework.data.annotation.Id

@Document("branches")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Branch(
  @Id
  var id: String? = null,
  var branch: String? = null,
  var active: Boolean = false,
  var tracksFileRenames: Boolean = false,
  var latestCommit: String? = null,

  @Relations(
    edges = [BranchFileConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  @JsonIgnoreProperties(value = ["branches"])
  var files: List<File>? = null
)
