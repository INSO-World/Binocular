package com.inso_world.binocular.web.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.web.entity.edge.BranchFileConnection
import com.inso_world.binocular.web.entity.edge.BranchFileFileConnection
import com.inso_world.binocular.web.entity.edge.CommitFileConnection
import com.inso_world.binocular.web.entity.edge.CommitFileUserConnection
import com.inso_world.binocular.web.entity.edge.ModuleFileConnection
import org.springframework.data.annotation.Id

@Document("files")
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
  var commits: List<Commit>? = null,

  @Relations(
    edges = [BranchFileConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  var branches: List<Branch>? = null,

  @Relations(
    edges = [ModuleFileConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.INBOUND
  )
  var modules: List<Module>? = null,

  @Relations(
    edges = [BranchFileFileConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  var relatedFiles: List<File>? = null,

  @Relations(
    edges = [CommitFileUserConnection::class],
    lazy = true,
    maxDepth = 1,
    direction = Relations.Direction.OUTBOUND
  )
  var users: List<User>? = null
)
