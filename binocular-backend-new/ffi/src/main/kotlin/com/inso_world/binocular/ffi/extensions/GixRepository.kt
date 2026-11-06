package com.inso_world.binocular.ffi.pojos

import com.inso_world.binocular.ffi.extensions.toFfi
import com.inso_world.binocular.ffi.internal.GixRepository
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository

internal fun Repository.toFfi(): GixRepository =
    GixRepository(
        gitDir = this.localPath,
        workTree = null,
        remotes = this.remotes.map { it.toFfi() },
    )

private fun normalizePath(path: String): String = if (path.endsWith(".git")) path else "$path/.git"

internal fun GixRepository.toModel(project: Project): Repository =
    Repository(
        localPath = normalizePath(gitDir),
        project = project,
    )
