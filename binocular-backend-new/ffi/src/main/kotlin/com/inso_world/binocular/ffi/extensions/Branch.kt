package com.inso_world.binocular.ffi.extensions

import com.inso_world.binocular.ffi.internal.BinocularBranch
import com.inso_world.binocular.model.Branch

internal fun BinocularBranch.toModel(): Branch =
    Branch(
        name = this.name.replace("refs/remotes/", "").replace("refs/heads/", ""),
    )
