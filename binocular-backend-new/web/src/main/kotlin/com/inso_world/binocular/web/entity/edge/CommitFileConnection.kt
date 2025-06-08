package com.inso_world.binocular.web.entity.edge

import com.arangodb.springframework.annotation.Edge
import com.arangodb.springframework.annotation.From
import com.arangodb.springframework.annotation.To
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.File
import org.springframework.data.annotation.Id

@Edge(value = "commits-files")
data class CommitFileConnection(
  @Id var id: String? = null,
  @From var from: Commit,
  @To var to: File,
  var lineCount: Int? = null
)
