package com.inso_world.binocular.ffi.extensions

import com.inso_world.binocular.ffi.internal.GixRemote
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.vcs.Remote

internal fun GixRemote.toModel(
    repository: Repository,
    remoteRegistry: MutableMap<String, Remote> = mutableMapOf(),
): Remote {
    val existing = remoteRegistry[this.name]
    return if (existing != null) {
        if (existing.url != this.url) {
            existing.url = this.url
        }
        existing
    } else {
        Remote(
            name = this.name,
            url = this.url,
            repositoryId = repository.iid
        ).also {
            remoteRegistry[this.name] = it
            repository.remoteIds.add(it.iid)
        }
    }
}

internal fun Remote.toFfi(): GixRemote = GixRemote(
    name = this.name,
    url = this.url,
)
