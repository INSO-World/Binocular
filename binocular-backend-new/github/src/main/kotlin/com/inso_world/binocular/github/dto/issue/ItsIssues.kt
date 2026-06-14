package com.inso_world.binocular.github.dto.issue

import com.inso_world.binocular.github.dto.PageInfo

data class ItsIssues(
    val totalCount: Int,
    val nodes: List<ItsGitHubIssue>,
    val pageInfo: PageInfo
)
