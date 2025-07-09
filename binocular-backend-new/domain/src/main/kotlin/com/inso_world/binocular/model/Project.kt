package com.inso_world.binocular.model

import jakarta.validation.constraints.NotBlank

class Project(
    var id: String? = null,
    @field:NotBlank
    val name: String,
    val issues: MutableSet<Issue> = mutableSetOf(),
    val description: String? = null,
    var repo: Repository? = null,
) {
    override fun toString(): String = "Project(id=$id, name='$name', description=$description)"
}
