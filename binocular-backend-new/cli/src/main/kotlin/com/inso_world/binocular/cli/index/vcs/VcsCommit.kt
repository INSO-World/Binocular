package com.inso_world.binocular.cli.index.vcs

import com.inso_world.binocular.ffi.pojos.BinocularCommitPojo
import com.inso_world.binocular.model.Commit
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
    val parents: Set<VcsCommit> = setOf(),
) {
    override fun toString(): String =
        "VcsCommit(sha='$sha', message='$message', branch=$branch, parents=$parents, commitTime=$commitTime, authorTime=$authorTime)"

    fun toDomain(): Commit =
        Commit(
            sha = this.sha,
            message = this.message,
            commitDateTime = this.commitTime ?: LocalDateTime.now(),
            authorDateTime = this.authorTime,
            committer = this.committer?.toEntity(),
            author = this.author?.toEntity(),
            parents = mutableSetOf(), // Will be set later in transformCommits
            branches = mutableSetOf(), // Will be set later in transformCommits
            repositoryId = null, // Will be set later in transformCommits
        )
}

fun List<BinocularCommitPojo>.toDtos(): List<VcsCommit> {
    // First, create all VcsCommit objects with empty parents
    val vcsCommits =
        this.map { pojo ->
            VcsCommit(
                sha = pojo.commit,
                message = Base64.getDecoder().decode(pojo.message).toString(StandardCharsets.UTF_8),
                branch = pojo.branch!!, // TODO avoid non null check
                commitTime =
                    pojo.committer?.let { ct ->
                        LocalDateTime.ofEpochSecond(ct.time.seconds, 0, ZoneOffset.UTC)
                    },
                authorTime =
                    pojo.author?.let { ct ->
                        LocalDateTime.ofEpochSecond(ct.time.seconds, 0, ZoneOffset.UTC)
                    },
                parents = emptySet(), // Will be set in the next step
                committer = pojo.committer?.toVcsPerson(),
                author = pojo.author?.toVcsPerson(),
            )
        }
    val vcsMap = vcsCommits.associateBy { it.sha }
    // Now, set the parents for each VcsCommit
    this.forEachIndexed { idx, pojo ->
        val vcs = vcsCommits[idx]
        val parentVcs = pojo.parents.mapNotNull { parentPojo -> vcsMap[parentPojo.commit] }.toSet()
        // Use reflection to set the parents property (since it's a val)
        val parentsField = VcsCommit::class.java.getDeclaredField("parents")
        parentsField.isAccessible = true
        parentsField.set(vcs, parentVcs)
    }
    return vcsCommits
}
