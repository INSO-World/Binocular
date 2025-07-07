package com.inso_world.binocular.cli.index.vcs

import com.inso_world.binocular.cli.entity.Project
import com.inso_world.binocular.cli.entity.Repository
import com.inso_world.binocular.ffi.pojos.BinocularRepositoryPojo

data class VcsRepository(
    val name: String,
) {
    fun toEntity(p: Project): Repository =
        Repository(
            name = name,
            project = p,
        )
}

fun BinocularRepositoryPojo.toVcsRepository(): VcsRepository =
    VcsRepository(
        name = this.gitDir,
    )
