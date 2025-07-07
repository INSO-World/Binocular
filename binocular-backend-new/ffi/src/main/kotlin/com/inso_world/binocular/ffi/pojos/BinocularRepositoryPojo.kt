package com.inso_world.binocular.ffi.pojos

import com.inso_world.binocular.internal.BinocularRepository

data class BinocularRepositoryPojo(
    val gitDir: String,
    val workTree: String?,
    val origin: BinocularRemotePojo? = null,
) {
    internal fun toFfi(): BinocularRepository =
        BinocularRepository(
            gitDir = gitDir,
            workTree = workTree,
            origin = origin?.toFfi(),
        )
}

internal fun BinocularRepository.toPojo(): BinocularRepositoryPojo =
    BinocularRepositoryPojo(
        gitDir = this.gitDir,
        workTree = workTree,
        origin = origin?.toPojo(),
    )
