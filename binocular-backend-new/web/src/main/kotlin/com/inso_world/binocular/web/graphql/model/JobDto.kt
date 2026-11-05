package com.inso_world.binocular.web.graphql.model

import java.time.LocalDateTime

data class JobDto(
    var id: String? = null,
    var name: String? = null,
    var status: String? = null,
    var stage: String? = null,
    var createdAt: LocalDateTime? = null,
    var finishedAt: LocalDateTime? = null,
    var webUrl: String? = null,
)
