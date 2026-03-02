package com.inso_world.binocular.web.graphql.model

data class MilestoneDto(
    var id: String? = null,
    var iid: Int? = null,
    var title: String? = null,
    var description: String? = null,
    var state: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null,
    var dueDate: String? = null,
    var startDate: String? = null,
    var webUrl: String? = null,
)
