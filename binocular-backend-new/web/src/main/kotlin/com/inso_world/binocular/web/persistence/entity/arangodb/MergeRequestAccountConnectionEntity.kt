package com.inso_world.binocular.web.persistence.entity.arangodb

import com.arangodb.springframework.annotation.Edge
import com.arangodb.springframework.annotation.From
import com.arangodb.springframework.annotation.To
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific entity for a connection between a MergeRequest and an Account.
 */
@Edge(value = "merge-requests-accounts")
data class MergeRequestAccountConnectionEntity(
  @Id var id: String? = null,
  @From var from: MergeRequestEntity,
  @To var to: AccountEntity
)
