package com.inso_world.binocular.cli.index.vcs

import com.inso_world.binocular.ffi.pojos.BinocularCommitSignaturePojo
import com.inso_world.binocular.model.User
import java.util.Objects

data class VcsPerson(
    val name: String,
    val email: String,
) {
    fun toEntity(): User =
        User(
            name = this.name,
            email = this.email,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VcsPerson

        if (name != other.name) return false
        if (email != other.email) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Objects.hashCode(name)
        result = 31 * result + Objects.hashCode(email)
        return result
    }
}

fun BinocularCommitSignaturePojo.toVcsPerson(): VcsPerson =
    VcsPerson(
        name = this.name,
        email = this.email,
    )
