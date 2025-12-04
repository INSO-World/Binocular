package com.inso_world.binocular.github.dto.issue

import com.inso_world.binocular.model.Issue
import java.time.LocalDateTime
import java.time.OffsetDateTime

data class ItsGitHubIssue(
    val id: String, // external GitHub id for issue
    val number: Int,
    val title: String,
    val body: String?, // description
    val state: String,
    val url: String,
    val closedAt: String?,
    val createdAt: String,
    val updatedAt: String?,
    val labels: ItsLabelWrapper?,
    val milestone: ItsMilestoneWrapper?,
    val author: ItsUser,
    val assignees: ItsAssigneeWrapper?,
    val timelineItems: ItsTimelineItemWrapper?
) {
    fun toDomain(): Issue {
        fun parseDateTime(dt: String): LocalDateTime =
            OffsetDateTime.parse(dt).toLocalDateTime()

        return Issue(
            gid = id,
            iid = number,
            title = title,
            description = body,
            createdAt = parseDateTime(createdAt),
            closedAt = closedAt?.let { parseDateTime(it) },
            updatedAt = updatedAt?.let { parseDateTime(it) },
            state = state,
            webUrl = url
            // TODO map labels, author etc
        )
    }
}

