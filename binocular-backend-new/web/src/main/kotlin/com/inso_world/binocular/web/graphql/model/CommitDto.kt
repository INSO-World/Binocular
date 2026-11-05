package com.inso_world.binocular.web.graphql.model

import java.time.LocalDateTime

data class CommitDto(
    var id: String? = null,
    var sha: String? = null,
    var authorDateTime: LocalDateTime? = null,
    var commitDateTime: LocalDateTime? = null,
    var message: String? = null,
    var webUrl: String? = null,
    var stats: StatsDto? = null,
    var branch: String? = null,
)
