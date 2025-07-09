package com.inso_world.binocular.cli.index.vcs

import com.inso_world.binocular.ffi.pojos.BinocularRepositoryPojo
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository

data class VcsRepository(
    val name: String,
) {
    fun toDomain(p: Project): Repository =
        Repository(
            id = null,
            name = name,
            projectId = p.id,
        )
}

internal fun BinocularRepositoryPojo.toVcsRepository(): VcsRepository =
    VcsRepository(
        name = this.gitDir,
    )
