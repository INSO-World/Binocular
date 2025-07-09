package com.inso_world.binocular.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * Domain model for a Commit, representing a commit in a Git repository.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class Commit(
    var id: String? = null,
    @field:NotBlank
    @field:Size(min = 40, max = 40)
    @field:NotNull
    var sha: String,
    @field:PastOrPresent
    var authorDateTime: LocalDateTime? = null,
    @field:PastOrPresent
    @field:NotNull
    val commitDateTime: LocalDateTime? = null,
    @field:NotBlank
    var message: String? = null,
    var author: User? = null,
    var committer: User? = null,
    var repository: Repository? = null,
    var webUrl: String? = null,
    var branch: String? = null,
    var stats: Stats? = null,
    // Relationships
    val branches: MutableSet<Branch> = mutableSetOf(),
    var parents: List<Commit> = emptyList(),
    var children: List<Commit> = emptyList(),
    var builds: List<Build> = emptyList(),
    var files: List<File> = emptyList(),
    var modules: List<Module> = emptyList(),
    var users: List<User> = emptyList(),
    var issues: List<Issue> = emptyList(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Commit

        if (id != other.id) return false
        if (sha != other.sha) return false
        if (authorDateTime != other.authorDateTime) return false
        if (commitDateTime != other.commitDateTime) return false
        if (message != other.message) return false
        if (author != other.author) return false
        if (committer != other.committer) return false
        if (repository != other.repository) return false
        if (webUrl != other.webUrl) return false
        if (branch != other.branch) return false
        if (stats != other.stats) return false
        if (branches != other.branches) return false
        if (parents != other.parents) return false
        if (children != other.children) return false
        if (builds != other.builds) return false
        if (files != other.files) return false
        if (modules != other.modules) return false
        if (users != other.users) return false
        if (issues != other.issues) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + sha.hashCode()
        result = 31 * result + (authorDateTime?.hashCode() ?: 0)
        result = 31 * result + (commitDateTime?.hashCode() ?: 0)
        result = 31 * result + (message?.hashCode() ?: 0)
        result = 31 * result + (webUrl?.hashCode() ?: 0)
        result = 31 * result + (branch?.hashCode() ?: 0)
        result = 31 * result + (stats?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "Commit(id=$id, sha='$sha', authorDateTime=$authorDateTime, commitDateTime=$commitDateTime, message=$message, webUrl=$webUrl, branch=$branch, stats=$stats, author=$author, committer=$committer)"
}

data class Stats(
    var additions: Long,
    var deletions: Long,
)
