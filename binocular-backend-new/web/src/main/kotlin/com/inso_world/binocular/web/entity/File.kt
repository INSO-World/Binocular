package com.inso_world.binocular.web.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.inso_world.binocular.web.entity.edge.BranchFileConnection
import com.inso_world.binocular.web.entity.edge.CommitFileConnection
import com.inso_world.binocular.web.entity.edge.ModuleFileConnection
import org.springframework.data.annotation.Id

@Document("files")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class File(
  @Id
  var id: String? = null,
  var path: String,
  var webUrl: String,
  var maxLength: Int? = null,

  @Relations(
    edges = [CommitFileConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  @JsonIgnoreProperties(value = ["files"])
  var commits: List<Commit>? = null,

  @Relations(
    edges = [BranchFileConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  @JsonIgnoreProperties(value = ["files"])
  var branches: List<Branch>? = null,

  @Relations(
    edges = [ModuleFileConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  @JsonIgnoreProperties(value = ["files"])
  var modules: List<Module>? = null
)
