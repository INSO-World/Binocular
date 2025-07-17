package com.inso_world.binocular.model

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

/**
 * Domain model for a User, representing a Git user.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
class User(
    var id: String? = null,
    @field:NotBlank val name: String? = null,
    @field:Email
    var email: String? = null,
//    var repositoryId: String? = null,
    @field:NotNull
    var repository: Repository? = null,
    // Relationships
    var committedCommits: MutableSet<Commit> = mutableSetOf(),
    var authoredCommits: MutableSet<Commit> = mutableSetOf(),
    val issues: List<Issue> = emptyList(),
    val files: List<File> = emptyList(),
) {
    fun uniqueKey(): String {
        if (repository == null) {
            throw IllegalStateException("cannot generate unique key for User when repository is null")
        }
        return "${repository?.name},$email"
    }

    fun addAuthoredCommit(commit: Commit) {
        authoredCommits.add(commit)
        commit.author = this
    }

//
//    fun addProjectMembership(member: ProjectMember) {
//        memberAliases.add(member)
//        member.user = this
//    }
//
    fun addCommittedCommit(commit: Commit) {
        committedCommits.add(commit)
        commit.committer = this
    }

    @Deprecated("do not use")
    val gitSignature: String
        get() = "$name <$email>"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (name != other.name) return false
        if (gitSignature != other.gitSignature) return false
        if (authoredCommits != other.authoredCommits) return false
        if (committedCommits != other.committedCommits) return false
        if (issues != other.issues) return false
        if (files != other.files) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + gitSignature.hashCode()
        return result
    }

    override fun toString(): String = "User(id=$id, name=$name, gitSignature=$gitSignature, repositoryId=${repository?.id})"
}
