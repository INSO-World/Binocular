package com.inso_world.binocular.web.graphql.model

import java.time.LocalDateTime

data class MentionDto(
    var commit: String? = null,
    var createdAt: LocalDateTime? = null,
    var closes: Boolean? = null,
)
