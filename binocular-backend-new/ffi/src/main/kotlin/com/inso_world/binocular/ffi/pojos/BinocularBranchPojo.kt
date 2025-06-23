package com.inso_world.binocular.ffi.pojos

import com.inso_world.binocular.internal.BinocularBranch

data class BinocularBranchPojo(
    var name: String,
    var commits: List<String>,
)

internal fun BinocularBranch.toPojo(): BinocularBranchPojo =
    BinocularBranchPojo(
        name,
        commits,
    )
