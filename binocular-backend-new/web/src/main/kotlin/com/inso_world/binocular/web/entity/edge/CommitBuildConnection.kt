package com.inso_world.binocular.web.entity.edge

import com.arangodb.springframework.annotation.Edge
import com.arangodb.springframework.annotation.From
import com.arangodb.springframework.annotation.To
import com.inso_world.binocular.web.entity.Build
import com.inso_world.binocular.web.entity.Commit
import org.springframework.data.annotation.Id

@Edge(value = "commits-builds")
data class CommitBuildConnection(
  @Id var id: String? = null,
  @From var from: Commit,
  @To var to: Build
)
