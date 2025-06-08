package com.inso_world.binocular.web.entity.edge

import com.arangodb.springframework.annotation.Edge
import com.arangodb.springframework.annotation.From
import com.arangodb.springframework.annotation.To
import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.entity.File
import org.springframework.data.annotation.Id

@Edge(value = "branches-files")
data class BranchFileConnection(
  @Id var id: String? = null,
  @From var from: Branch,
  @To var to: File
)
