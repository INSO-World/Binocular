package com.inso_world.binocular.web.graphql.mapper.impl

import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.web.graphql.model.IssueDto
import org.springframework.stereotype.Component

@Component
class GraphQlIssueMapper(
    private val mentionMapper: GraphQlMentionMapper
) {
    fun toDto(issue: Issue): IssueDto {
        return IssueDto(
            id = issue.id,
            iid = issue.iid,
            title = issue.title,
            description = issue.description,
            createdAt = issue.createdAt,
            closedAt = issue.closedAt,
            updatedAt = issue.updatedAt,
            labels = issue.labels,
            state = issue.state,
            webUrl = issue.webUrl,
            mentions = issue.mentions.map { mentionMapper.toDto(it) }
        )
    }
}
