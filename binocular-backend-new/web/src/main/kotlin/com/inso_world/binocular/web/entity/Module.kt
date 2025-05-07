package com.inso_world.binocular.web.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.inso_world.binocular.web.entity.edge.CommitModuleConnection
import com.inso_world.binocular.web.entity.edge.ModuleFileConnection
import com.inso_world.binocular.web.entity.edge.ModuleModuleConnection
import org.springframework.data.annotation.Id

@Document("modules")
data class Module(
  @Id
  var id: String? = null,
  var path: String,

  @Relations(
    edges = [CommitModuleConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  @JsonIgnoreProperties(value = ["modules"])
  var commits: List<Commit>? = null,

  @Relations(
    edges = [ModuleFileConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  @JsonIgnoreProperties(value = ["modules"])
  var files: List<File>? = null,

  @Relations(
    edges = [ModuleModuleConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  @JsonIgnoreProperties(value = ["parentModules", "childModules"])
  var childModules: List<Module>? = null,

  @Relations(
    edges = [ModuleModuleConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  @JsonIgnoreProperties(value = ["parentModules", "childModules"])
  var parentModules: List<Module>? = null
)
