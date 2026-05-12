package com.inso_world.binocular.web.graphql.model

import com.inso_world.binocular.model.Stats

data class StatsDto(
    var additions: Long,
    var deletions: Long,
    var kind: Stats.StatsKind? = null,
)
