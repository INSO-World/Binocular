package com.inso_world.binocular.cli.index.vcs

import com.inso_world.binocular.ffi.pojos.BinocularBranchPojo
import com.inso_world.binocular.model.Branch

data class VcsBranch(
    val name: String,
) {
    fun toEntity(): Branch =
        Branch(
            name = this.name,
        )
}

fun BinocularBranchPojo.toVcsBranch(): VcsBranch =
    VcsBranch(
        name = this.name,
    )
