package com.inso_world.binocular.ffi.pojos

import com.inso_world.binocular.internal.ThreadSafeRepository

data class BinocularRepositoryPojo(
    val gitDir: String,
    val workTree: String?,
    val commonDir: String?,
) {
    internal fun toFfi(): ThreadSafeRepository =
        ThreadSafeRepository(
            gitDir = gitDir,
            commonDir = commonDir,
            workTree = workTree,
        )
}

internal fun ThreadSafeRepository.toPojo(): BinocularRepositoryPojo =
    BinocularRepositoryPojo(
        gitDir = this.gitDir,
        workTree = this.workTree,
        commonDir = this.commonDir,
    )
