package com.inso_world.binocular.ffi.pojos

import com.inso_world.binocular.internal.BinocularBranch
import java.util.Objects

data class BinocularBranchPojo(
    var name: String,
    var commits: List<String>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BinocularBranchPojo

        if (name != other.name) return false
        if (commits != other.commits) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Objects.hashCode(name)
        result = 31 * result + Objects.hashCode(commits)
        return result
    }
}

internal fun BinocularBranch.toPojo(): BinocularBranchPojo =
    BinocularBranchPojo(
        name,
        commits,
    )
