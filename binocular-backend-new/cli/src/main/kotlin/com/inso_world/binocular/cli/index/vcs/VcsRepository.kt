package com.inso_world.binocular.cli.index.vcs

import com.inso_world.binocular.cli.entity.Repository
import com.inso_world.binocular.ffi.BinocularRepositoryPojo

data class VcsRepository(
    val name: String,
) {
    fun toEntity(): Repository =
        Repository(
            name = name,
        )
}

fun BinocularRepositoryPojo.toVcsRepository(): VcsRepository =
    VcsRepository(
        name = this.gitDir,
    )
