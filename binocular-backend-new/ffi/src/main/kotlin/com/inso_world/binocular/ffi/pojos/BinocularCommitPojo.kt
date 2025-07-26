package com.inso_world.binocular.ffi.pojos

import com.inso_world.binocular.internal.BinocularCommitVec

data class BinocularCommitPojo(
    var commit: String,
    var message: String,
    var committer: BinocularCommitSignaturePojo?,
    var author: BinocularCommitSignaturePojo?,
    var branch: String?,
    var parents: MutableSet<BinocularCommitPojo>,
)

internal fun BinocularCommitVec.toPojo(): BinocularCommitPojo =
    BinocularCommitPojo(
        commit = commit,
        message = message,
        committer = committer?.toPojo(),
        author = author?.toPojo(),
        branch = branch,
        parents = mutableSetOf(),
    )
