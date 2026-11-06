package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document

/**
 * Stats entity for ArangoDB based on the domain model.
 */
@Document("stats")
data class StatsEntity (
    var additions: Long,
    var deletions: Long,
)
