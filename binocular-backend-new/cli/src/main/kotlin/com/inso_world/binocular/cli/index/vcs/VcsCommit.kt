package com.inso_world.binocular.cli.index.vcs

import com.inso_world.binocular.ffi.pojos.BinocularCommitPojo
import com.inso_world.binocular.model.Commit
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Objects
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

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

    override fun hashCode(): Int = Objects.hash(sha, message, branch, commitTime, authorTime)
}

fun List<BinocularCommitPojo>.toDtos(): List<VcsCommit> {
    val commitCache = ConcurrentHashMap<String, VcsCommit>()

    // First pass: Create VcsCommit objects without parents
    parallelStream().forEach { pojo ->
        commitCache.computeIfAbsent(pojo.commit) { sha ->
            VcsCommit(
                sha = sha,
                message = pojo.message,
                branch = pojo.branch ?: "",
                committer = pojo.committer?.let { VcsPerson(it.name, it.email) },
                author = pojo.author?.let { VcsPerson(it.name, it.email) },
                commitTime = pojo.committer?.time?.let { t -> LocalDateTime.ofEpochSecond(t.seconds, abs(t.offset), ZoneOffset.UTC) },
                authorTime = pojo.author?.time?.let { t -> LocalDateTime.ofEpochSecond(t.seconds, abs(t.offset), ZoneOffset.UTC) },
                parents = mutableSetOf(), // Will be populated in second pass
            )
        }
    }

    // Second pass: Set up parent relationships using cached objects
    parallelStream().forEach { pojo ->
        val commit = commitCache[pojo.commit]!!
        val parentCommits =
            pojo.parents
                .mapNotNull { parent ->
                    commitCache[parent.commit]
                }.toSet()

        commit.parents.addAll(parentCommits)
    }

    return parallelStream().map { pojo -> commitCache[pojo.commit]!! }.toList()
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
