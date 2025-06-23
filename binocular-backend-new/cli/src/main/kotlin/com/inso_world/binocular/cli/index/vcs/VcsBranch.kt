package com.inso_world.binocular.cli.index.vcs

import com.inso_world.binocular.cli.entity.Branch
import com.inso_world.binocular.internal.BinocularBranch

data class VcsBranch(
    val name: String,
) {
    fun toEntity(): Branch =
        Branch(
            name = this.name,
        )
}

fun BinocularBranch.toVcsBranch(): VcsBranch =
    VcsBranch(
        name = this.name,
    )
