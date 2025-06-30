package com.inso_world.binocular.web.persistence.entity.arangodb

import com.arangodb.springframework.annotation.Edge
import com.arangodb.springframework.annotation.From
import com.arangodb.springframework.annotation.To
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific entity for a connection between an Issue and an Account.
 */
@Edge(value = "issues-accounts")
data class IssueAccountConnectionEntity(
  @Id var id: String? = null,
  @From var from: IssueEntity,
  @To var to: AccountEntity
)
