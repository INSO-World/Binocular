package com.inso_world.binocular.ffi.pojos

import com.inso_world.binocular.internal.BinocularCommitVec
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.Objects

data class BinocularCommitPojo(
    var commit: String,
    var message: String,
    var committer: BinocularCommitSignaturePojo?,
    var author: BinocularCommitSignaturePojo?,
    var branch: String?,
    var parents: MutableSet<BinocularCommitPojo>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BinocularCommitPojo

        if (commit != other.commit) return false
        if (message != other.message) return false
        if (committer != other.committer) return false
        if (author != other.author) return false
        if (branch != other.branch) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Objects.hashCode(commit)
        result = 31 * result + Objects.hashCode(message)
        result = 31 * result + Objects.hashCode(committer)
        result = 31 * result + Objects.hashCode(author)
        result = 31 * result + Objects.hashCode(branch)
        return result
    }

    override fun toString(): String =
        "BinocularCommitPojo(commit='$commit', message='$message', committer=$committer, author=$author, branch=$branch)"
}

internal fun BinocularCommitVec.toPojo(): BinocularCommitPojo =
    BinocularCommitPojo(
        commit = commit,
        message = Base64.getDecoder().decode(message.toByteArray(StandardCharsets.UTF_8)).toString(StandardCharsets.UTF_8),
        committer = committer?.toPojo(),
        author = author?.toPojo(),
        branch = branch,
        parents = mutableSetOf(),
    )
