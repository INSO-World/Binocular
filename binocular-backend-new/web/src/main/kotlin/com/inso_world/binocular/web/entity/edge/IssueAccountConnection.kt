package com.inso_world.binocular.web.entity.edge

import com.arangodb.springframework.annotation.Edge
import com.arangodb.springframework.annotation.From
import com.arangodb.springframework.annotation.To
import org.springframework.data.annotation.Id

@Edge(value = "`issues-accounts`")
data class IssueAccountConnection(
  @Id var id: String? = null,
  @From var from: String? = null,
  @To var to: String? = null
)
