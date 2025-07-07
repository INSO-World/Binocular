package com.inso_world.binocular.cli.index.vcs

import com.inso_world.binocular.cli.entity.Commit
import com.inso_world.binocular.ffi.pojos.BinocularCommitPojo
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Base64

data class VcsCommit(
    val sha: String,
    val message: String,
    val branch: String,
    val committer: VcsPerson?,
    val author: VcsPerson?,
    val commitTime: LocalDateTime?,
    val authorTime: LocalDateTime?,
    val parents: List<String>?,
) {
    override fun toString(): String =
        "VcsCommit(sha='$sha', message='$message', branch=$branch, parents=$parents, commitTime=$commitTime, authorTime=$authorTime)"

    fun toEntity(): Commit =
        Commit(
            sha = this.sha,
            message = this.message,
            commitTime = this.commitTime ?: LocalDateTime.now(),
            authorTime = this.authorTime,
            committer = this.committer?.toEntity(),
            author = this.author?.toEntity(),
            parents = emptyList(), // Will be set later in transformCommits
            branches = mutableSetOf(), // Will be set later in transformCommits
            repository = null, // Will be set later in transformCommits
        )
}

fun BinocularCommitPojo.toDto(): VcsCommit =
    VcsCommit(
        sha = this.commit,
        message = Base64.getDecoder().decode(this.message).toString(StandardCharsets.UTF_8),
        branch = this.branch!!, // TODO avoid non null check
        commitTime =
            this.committer?.let { ct ->
                LocalDateTime.ofEpochSecond(ct.time.seconds, 0, ZoneOffset.UTC)
            },
        authorTime =
            this.author?.let { ct ->
                LocalDateTime.ofEpochSecond(ct.time.seconds, 0, ZoneOffset.UTC)
            },
        parents = this.parents.ifEmpty { null },
        committer = this.committer?.toVcsPerson(),
        author = this.author?.toVcsPerson(),
    )
