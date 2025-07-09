package com.inso_world.binocular.cli.index.vcs

import com.inso_world.binocular.ffi.pojos.BinocularRepositoryPojo
import com.inso_world.binocular.model.Repository

data class VcsRepository(
    val name: String,
) {
    fun toDomain(): Repository =
        Repository(
            id = null,
            name = name,
//            project = p,
        )
}

internal fun BinocularRepositoryPojo.toVcsRepository(): VcsRepository =
    VcsRepository(
        name = this.gitDir,
    )
