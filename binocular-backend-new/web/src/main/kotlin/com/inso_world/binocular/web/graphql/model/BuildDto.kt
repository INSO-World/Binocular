package com.inso_world.binocular.web.graphql.model

import java.time.LocalDateTime

data class BuildDto(
    var id: String? = null,
    var sha: String? = null,
    var ref: String? = null,
    var status: String? = null,
    var tag: String? = null,
    var user: String? = null,
    var userFullName: String? = null,
    var createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null,
    var startedAt: LocalDateTime? = null,
    var finishedAt: LocalDateTime? = null,
    var committedAt: LocalDateTime? = null,
    var duration: Int? = null,
    var webUrl: String? = null,
    var jobs: List<JobDto> = emptyList(),
)
