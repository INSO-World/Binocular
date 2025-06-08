package com.inso_world.binocular.web.entity.edge

import com.arangodb.springframework.annotation.Edge
import com.arangodb.springframework.annotation.From
import com.arangodb.springframework.annotation.To
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.User
import org.springframework.data.annotation.Id

@Edge(value = "issues-users")
data class IssueUserConnection(
  @Id var id: String? = null,
  @From var from: Issue,
  @To var to: User
)
