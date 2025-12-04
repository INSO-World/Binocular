package com.inso_world.binocular.github.dto.user

import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Platform

data class ItsGitHubUser(
    val id: String, // GitHub-ID for user
    val login: String,
    val email: String?,
    val name: String?,
    val url: String?,
    val avatarUrl: String?
) {
    fun toDomain(): Account {
        val acc = Account(
            gid = this.id,
            login = this.login,
            name = this.name,
            url = this.url,
            avatarUrl = this.avatarUrl,
            platform = Platform.GitHub
        )
        return acc
    }
}
