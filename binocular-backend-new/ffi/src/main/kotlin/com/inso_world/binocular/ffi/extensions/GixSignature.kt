package com.inso_world.binocular.ffi.extensions

import com.inso_world.binocular.ffi.internal.GixSignature
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature

internal fun GixSignature.toDeveloper(repositoryId: Repository.Id): Developer {
    val nameTrimmed = this.name.toString().trim()
    val emailTrimmed = this.email.toString().trim()
    require(nameTrimmed.isNotBlank()) { "Signature name must not be blank" }
    require(emailTrimmed.isNotBlank()) { "Signature email must not be blank" }

    return Developer(
        name = nameTrimmed,
        email = emailTrimmed,
        repositoryId = repositoryId,
    )
}

internal fun GixSignature.toSignature(repositoryId: Repository.Id): Signature =
    Signature(
        developerId = this.toDeveloper(repositoryId).iid,
        timestamp = this.time.toLocalDateTime(),
    )
