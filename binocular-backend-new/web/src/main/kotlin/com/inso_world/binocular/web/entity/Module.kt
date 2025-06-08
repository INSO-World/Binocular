package com.inso_world.binocular.web.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
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
  var commits: List<Commit>? = null,

  @Relations(
    edges = [ModuleFileConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  var files: List<File>? = null,

  @Relations(
    edges = [ModuleModuleConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  var childModules: List<Module>? = null,

  @Relations(
    edges = [ModuleModuleConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  var parentModules: List<Module>? = null
)
