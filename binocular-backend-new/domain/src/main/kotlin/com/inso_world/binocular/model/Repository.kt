package com.inso_world.binocular.model

import jakarta.validation.constraints.NotBlank

data class Repository(
    val id: String?,
    @field:NotBlank
    val name: String,
    val commits: MutableSet<Commit> = mutableSetOf(),
    val user: MutableSet<User> = mutableSetOf(),
    val branches: MutableSet<Branch> = mutableSetOf(),
//    @field:NotNull
//    val project: Project,
) {
    override fun toString(): String = "Repository(id=$id, name='$name')"
}
