package com.inso_world.binocular.ffi.pojos

import com.inso_world.binocular.ffi.internal.BinocularRepository
import com.inso_world.binocular.model.Repository

internal fun Repository.toFfi(): BinocularRepository =
    BinocularRepository(
        gitDir = this.localPath,
        workTree = null,
        origin = null,
    )

private fun normalizePath(path: String): String = if (path.endsWith(".git")) path else "$path/.git"

internal fun BinocularRepository.toModel(): Repository =
    Repository(
        localPath = normalizePath(gitDir),
//        workTree = workTree,
//        origin = origin?.toPojo(),
    )
