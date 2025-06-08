package com.inso_world.binocular.web.entity.edge

import com.arangodb.springframework.annotation.Edge
import com.arangodb.springframework.annotation.From
import com.arangodb.springframework.annotation.To
import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.MergeRequest
import org.springframework.data.annotation.Id

@Edge(value = "merge-requests-accounts")
data class MergeRequestAccountConnection(
  @Id var id: String? = null,
  @From var from: MergeRequest,
  @To var to: Account
)
