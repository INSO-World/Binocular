package com.inso_world.binocular.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

class Repository(
//    TODO switch to Any?
    val id: String?,
    @field:NotBlank
    val name: String,
    var commits: MutableSet<Commit> = mutableSetOf(),
    val user: MutableSet<User> = mutableSetOf(),
    val branches: MutableSet<Branch> = mutableSetOf(),
    @field:NotNull
    var projectId: String? = null,
//    var project: Project,
) {
    override fun toString(): String = "Repository(id=$id, name='$name')"
}
