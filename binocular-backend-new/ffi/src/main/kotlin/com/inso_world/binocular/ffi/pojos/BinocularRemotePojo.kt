package com.inso_world.binocular.ffi.pojos

import com.inso_world.binocular.internal.RepositoryRemote
import java.util.Objects

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BinocularRemotePojo

        if (name != other.name) return false
        if (url != other.url) return false
        if (path != other.path) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Objects.hashCode(name)
        result = 31 * result + Objects.hashCode(url)
        result = 31 * result + Objects.hashCode(path)
        return result
    }

    override fun toString(): String = "BinocularRemotePojo(name=$name, url=$url, path=$path)"
}

fun RepositoryRemote.toPojo(): BinocularRemotePojo =
    BinocularRemotePojo(
        name = this.name,
        url = this.url,
        path = this.path,
    )
