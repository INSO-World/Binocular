package com.inso_world.binocular.ffi.extensions

import com.inso_world.binocular.ffi.internal.GixSignature
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Signature

internal fun GixSignature.toDeveloper(
    developerRegistry: MutableMap<String, Developer> = mutableMapOf(),
): Developer {
    val nameTrimmed = this.name.trim()
    val emailTrimmed = this.email.trim()
    require(nameTrimmed.isNotBlank()) { "Signature name must not be blank" }
    require(emailTrimmed.isNotBlank()) { "Signature email must not be blank" }

    val gitSignature = "${nameTrimmed} "
    val existing = developerRegistry.values.firstOrNull { it.gitSignature == gitSignature }
    if (existing != null) {
        return existing
    }

    val dev = Developer(
        name = nameTrimmed,
        email = emailTrimmed,
    )
    developerRegistry[gitSignature] = dev
    return dev
}

internal fun GixSignature.toSignature(
    developerRegistry: MutableMap<String, Developer> = mutableMapOf(),
): Signature =
    Signature(
        developerId = this.toDeveloper(developerRegistry).iid,
        gitSignature = "${this.name.trim()} ",
        timestamp = this.time.toLocalDateTime()
    )
