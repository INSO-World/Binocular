package com.inso_world.binocular.cli.index.vcs

import com.inso_world.binocular.ffi.pojos.BinocularCommitSignaturePojo
import com.inso_world.binocular.model.User

data class VcsPerson(
    val name: String,
    val email: String,
) {
    fun toEntity(): User =
        User(
            name = this.name,
            email = this.email,
        )
}

fun BinocularCommitSignaturePojo.toVcsPerson(): VcsPerson =
    VcsPerson(
        name = this.name,
        email = this.email,
    )
