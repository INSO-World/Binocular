package com.inso_world.binocular.cli.index.vcs

import com.inso_world.binocular.model.Commit
import java.time.LocalDateTime
import java.util.Objects

@Deprecated("legacy")
data class VcsCommit(
    val sha: String,
    val message: String,
    val branch: String,
    val committer: VcsPerson?,
    val author: VcsPerson?,
    val commitTime: LocalDateTime?,
    val authorTime: LocalDateTime?,
    val parents: MutableSet<VcsCommit> = mutableSetOf(),
) {
    override fun toString(): String =
        "VcsCommit(sha='$sha', message='$message', branch=$branch, parents=${parents.map {
            it.sha
        }}, commitTime=$commitTime, authorTime=$authorTime)"

    fun toDomain(): Commit {
        val cmt =
            Commit(
                sha = this.sha,
                message = this.message,
                commitDateTime = this.commitTime ?: LocalDateTime.now(),
                authorDateTime = this.authorTime,
            )
        return cmt
    }

    override fun hashCode(): Int = Objects.hash(sha, message, branch, commitTime, authorTime)
}

fun traverseGraph(
    cmt: VcsCommit,
    visited: MutableSet<String> = mutableSetOf(),
): List<String> {
    if (!visited.add(cmt.sha)) {
        return emptyList() // Skip if we've already visited this commit
    }
    return listOf(cmt.sha) + cmt.parents.flatMap { traverseGraph(it, visited) }
}
