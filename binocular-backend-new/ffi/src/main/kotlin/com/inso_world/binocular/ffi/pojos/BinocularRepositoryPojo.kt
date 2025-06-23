package com.inso_world.binocular.ffi

import com.inso_world.binocular.internal.ThreadSafeRepository

data class BinocularRepositoryPojo(
    val gitDir: String,
    val workTree: String?,
    val commonDir: String?,
) {
    fun toFfi(): ThreadSafeRepository =
        ThreadSafeRepository(
            gitDir = gitDir,
            commonDir = commonDir,
            workTree = workTree,
        )
}

fun ThreadSafeRepository.toPojo(): BinocularRepositoryPojo =
    BinocularRepositoryPojo(
        gitDir = this.gitDir,
        workTree = this.workTree,
        commonDir = this.commonDir,
    )
