package com.inso_world.binocular.ffi.pojos

import com.inso_world.binocular.internal.BinocularRepository
import java.util.Objects

data class BinocularRepositoryPojo(
    val gitDir: String,
    val workTree: String?,
    val origin: BinocularRemotePojo? = null,
) {
    internal fun toFfi(): BinocularRepository =
        BinocularRepository(
            gitDir = gitDir,
            workTree = workTree,
            origin = origin?.toFfi(),
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BinocularRepositoryPojo

        if (gitDir != other.gitDir) return false
        if (workTree != other.workTree) return false
        if (origin != other.origin) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Objects.hashCode(gitDir)
        result = 31 * result + Objects.hashCode(workTree)
        result = 31 * result + Objects.hashCode(origin)
        return result
    }

    override fun toString(): String = "BinocularRepositoryPojo(gitDir='$gitDir', workTree=$workTree, origin=$origin)"
}

internal fun BinocularRepository.toPojo(): BinocularRepositoryPojo =
    BinocularRepositoryPojo(
        gitDir = this.gitDir,
        workTree = workTree,
        origin = origin?.toPojo(),
    )
