package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.MentionAccountConnectionEntity
import java.util.Date

/**
 * Mention entity for ArangoDB based on the domain model.
 */
@Document("mentions")
data class MentionEntity(
    var commit: String? = null,
    var createdAt: Date? = null,
    var closes: Boolean? = null,
    @Relations(
        edges = [MentionAccountConnectionEntity::class],
        direction = Relations.Direction.OUTBOUND,
        lazy = true,
        maxDepth = 1
    )
    var actor: AccountEntity? = null
)
