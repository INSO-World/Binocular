package com.inso_world.binocular.ffi.pojos

import com.inso_world.binocular.internal.RepositoryRemote

data class BinocularRemotePojo(
    var name: String? = null,
    var url: String? = null,
    var path: String? = null,
) {
    internal fun toFfi(): RepositoryRemote =
        RepositoryRemote(
            name = name,
            url = url,
            path = path,
        )
}

fun RepositoryRemote.toPojo(): BinocularRemotePojo =
    BinocularRemotePojo(
        name = this.name,
        url = this.url,
        path = this.path,
    )
