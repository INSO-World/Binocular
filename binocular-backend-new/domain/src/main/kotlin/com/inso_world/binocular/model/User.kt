package com.inso_world.binocular.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

/**
 * Domain model for a User, representing a Git user.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class User(
    var id: String? = null,
    @field:NotBlank
    val name: String? = null,
    @field:NotNull
    @field:NotBlank
    val gitSignature: String,
    // Relationships
    val authoredCommits: MutableSet<Commit> = mutableSetOf(),
    val committedCommits: MutableSet<Commit> = mutableSetOf(),
    val issues: List<Issue> = emptyList(),
    val files: List<File> = emptyList(),
) {
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

    override fun toString(): String = "User(id=$id, name=$name, gitSignature='$gitSignature')"
}
