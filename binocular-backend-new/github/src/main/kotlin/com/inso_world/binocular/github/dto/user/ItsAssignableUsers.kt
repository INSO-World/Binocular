package com.inso_world.binocular.github.dto.user

import com.inso_world.binocular.github.dto.PageInfo

data class ItsAssignableUsers(
    val nodes: List<ItsGitHubUser>,
    val pageInfo: PageInfo
)
