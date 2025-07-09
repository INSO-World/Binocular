package com.inso_world.binocular.model

import jakarta.validation.constraints.NotBlank
import org.jetbrains.annotations.NotNull

/**
 * Domain model for a Branch, representing a branch in a Git repository.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
class Branch(
    val id: String? = null,
    @field:NotBlank
    val name: String,
//    TODO remove 'branch' in favor of name
//    val branch: String? = null,
    val active: Boolean = false,
    val tracksFileRenames: Boolean = false,
    val latestCommit: String? = null,
    // Relationships
    val files: List<File> = emptyList(),
    val commits: MutableSet<Commit> = mutableSetOf(),
    @field:NotNull
    val repository: Repository? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Branch

        if (active != other.active) return false
        if (tracksFileRenames != other.tracksFileRenames) return false
        if (id != other.id) return false
        if (name != other.name) return false
        if (latestCommit != other.latestCommit) return false
        if (files != other.files) return false
        if (commits != other.commits) return false
        if (repository != other.repository) return false

        return true
    }

    override fun hashCode(): Int {
        var result = active.hashCode()
        result = 31 * result + tracksFileRenames.hashCode()
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + name.hashCode()
        result = 31 * result + (latestCommit?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "Branch(id=$id, name='$name', active=$active, tracksFileRenames=$tracksFileRenames, latestCommit=$latestCommit)"
}
