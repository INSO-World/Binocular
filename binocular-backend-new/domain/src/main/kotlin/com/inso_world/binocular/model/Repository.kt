package com.inso_world.binocular.model

import com.inso_world.binocular.model.validation.RepositoryValidation
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

@RepositoryValidation
class Repository(
//    TODO switch to Any?
    val id: String? = null,
    @field:NotBlank
    val name: String,
    var commits: MutableSet<@Valid Commit> = mutableSetOf(),
    val user: MutableSet<User> = mutableSetOf(),
    val branches: MutableSet<Branch> = mutableSetOf(),
    //    @field:NotNull // TODO conditional validation, only when coming out of infra
    var projectId: String? = null,
    var project: Project? = null, // TODO remove
) {
    override fun toString(): String = "Repository(id=$id, name='$name')"
}
