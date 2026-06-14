package com.inso_world.binocular.github.dto.issue

data class ItsMilestone(
    val id: String?,
    val url: String?,
    val number: Int?,
    val state: String?,
    val title: String,
    val description: String?,
    val creator: ItsUser,
    val createdAt: String?,
    val updatedAt: String?,
    val closedAt: String?,
    val dueOn: String?,
)
