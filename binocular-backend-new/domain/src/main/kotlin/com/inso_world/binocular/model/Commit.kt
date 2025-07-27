package com.inso_world.binocular.model

import com.inso_world.binocular.model.validation.FromInfrastructure
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * Domain model for a Commit, representing a commit in a Git repository.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class Commit(
    val id: String? = null,
    @field:Size(min = 40, max = 40)
    val sha: String,
    @field:PastOrPresent
    val authorDateTime: LocalDateTime? = null,
    @field:PastOrPresent
    @field:NotNull
    val commitDateTime: LocalDateTime? = null,
//    @field:NotBlank // commits may have not message
    val message: String? = null,
    var author: User? = null,
    var committer: User? = null,
//    var repository: Repository? = null, // TODO remove
    @field:NotNull(
        groups = [FromInfrastructure::class],
        message = "repositoryId must not be null when returning from infrastructure layer",
    )
    var repositoryId: String? = null,
    val webUrl: String? = null,
    @Deprecated("do not use")
    val branch: String? = null,
    val stats: Stats? = null,
    // Relationships
    var branches: MutableSet<Branch> = mutableSetOf(),
    var parents: MutableSet<Commit> = mutableSetOf(),
    var children: MutableSet<Commit> = mutableSetOf(),
//    old stuff
    val builds: List<Build> = emptyList(),
    val files: List<File> = emptyList(),
    val modules: List<Module> = emptyList(),
    val issues: List<Issue> = emptyList(),
) : Cloneable {
    @Deprecated("Do not use")
    val users: List<User>
        get() =
            mutableListOf<User>()
                .let { lst ->
                    author?.let { lst.add(it) }
                    committer?.let { lst.add(it) }
                    lst
                }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false

        other as Commit

        if (id != other.id) return false
        if (sha != other.sha) return false
        if (authorDateTime != other.authorDateTime) return false
        if (commitDateTime != other.commitDateTime) return false
        if (message != other.message) return false
        if (repositoryId != other.repositoryId) return false
        if (webUrl != other.webUrl) return false
        if (stats != other.stats) return false
//        if (branches != other.branches) return false
//        if (parents != other.parents) return false
//        if (children != other.children) return false
//        if (builds != other.builds) return false
//        if (files != other.files) return false
//        if (modules != other.modules) return false
//        if (issues != other.issues) return false

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
        "Commit(id=$id, sha='$sha', authorDateTime=$authorDateTime, commitDateTime=$commitDateTime, message=$message, webUrl=$webUrl, stats=$stats, author=${author?.name}, committer=${committer?.name}, repositoryId=$repositoryId)"
}

data class Stats(
    var additions: Long,
    var deletions: Long,
)
