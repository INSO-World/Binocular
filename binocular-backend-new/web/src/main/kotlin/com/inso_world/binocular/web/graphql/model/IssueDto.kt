package com.inso_world.binocular.web.graphql.model

import java.time.LocalDateTime

data class IssueDto(
    var id: String? = null,
    var iid: Int? = null,
    var title: String? = null,
    var description: String? = null,
    var createdAt: LocalDateTime? = null,
    var closedAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null,
    var labels: List<String> = emptyList(),
    var state: String? = null,
    var webUrl: String? = null,
    var mentions: List<MentionDto> = emptyList(),
    var authorId: String? = null,
    var accountIds: Set<String> = emptySet(),
    var commitIds: Set<String> = emptySet(),
    var milestoneIds: Set<String> = emptySet(),
    var noteIds: Set<String> = emptySet(),
)
