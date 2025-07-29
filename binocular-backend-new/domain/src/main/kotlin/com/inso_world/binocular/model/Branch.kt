package com.inso_world.binocular.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.util.Objects

/**
 * Domain model for a Branch, representing a branch in a Git repository.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class Branch(
    val id: String? = null,
    @field:NotBlank
    val name: String,
    val active: Boolean = false,
    val tracksFileRenames: Boolean = false,
    val latestCommit: String? = null,
    // Relationships
    val files: List<File> = emptyList(),
    @field:NotEmpty
    val commits: MutableSet<Commit> = mutableSetOf(),
    @field:NotNull
    var repository: Repository? = null,
) {
    @Deprecated("legacy, use name property instead", replaceWith = ReplaceWith("name"))
    val branch: String = name

    fun uniqueKey(): String {
        val repo = repository
        if (repo == null) {
            throw IllegalStateException("Cannot generate unique key for $javaClass when repository is null")
        }
        return "${repo.name},$name"
    }

    fun addCommit(commit: Commit): Boolean {
        val a = this.commits.add(commit)
        val c =
            if (commit.parents.isNotEmpty()) {
                this.commits.addAll(commit.parents)
            } else {
                true
            }
        val b = commit.branches.add(this)
        return a && b && c
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Branch

        if (active != other.active) return false
        if (tracksFileRenames != other.tracksFileRenames) return false
        if (id != other.id) return false
        if (name != other.name) return false
        if (latestCommit != other.latestCommit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Objects.hashCode(active)
        result = 31 * result + Objects.hashCode(tracksFileRenames)
        result = 31 * result + Objects.hashCode(id)
        result = 31 * result + Objects.hashCode(name)
        result = 31 * result + Objects.hashCode(latestCommit)
        return result
    }

    override fun toString(): String =
        "Branch(id=$id, name='$name', active=$active, tracksFileRenames=$tracksFileRenames, latestCommit=$latestCommit, commitShas=${commits.map {
            it.sha
        }}, repositoryId=${repository?.id})"
}
