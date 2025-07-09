package com.inso_world.binocular.model

import jakarta.validation.constraints.NotBlank

class Project(
    id: String?,
    name: String,
    issues: MutableSet<Issue> = mutableSetOf(),
    description: String? = null,
) {
    var id: String? = id

    @field:NotBlank
    val name: String = name

    val issues: MutableSet<Issue> = issues

    val description: String? = description

    var repo: Repository? = null

    override fun toString(): String = "Project(id=$id, name='$name', description=$description)"
}
