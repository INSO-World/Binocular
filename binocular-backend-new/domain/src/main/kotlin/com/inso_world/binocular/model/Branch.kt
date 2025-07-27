package com.inso_world.binocular.model

import com.inso_world.binocular.model.validation.FromInfrastructure
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
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
    val commitShas: MutableSet<
        @Size(min = 40, max = 40)
        String,
    > = mutableSetOf(),
    @field:NotNull(groups = [FromInfrastructure::class])
    var repositoryId: String? = null,
) {
    @Deprecated("legacy, use name property instead", replaceWith = ReplaceWith("name"))
    val branch: String = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Branch

        if (active != other.active) return false
        if (tracksFileRenames != other.tracksFileRenames) return false
        if (id != other.id) return false
        if (name != other.name) return false
        if (latestCommit != other.latestCommit) return false
//        if (files != other.files) return false
        if (commitShas != other.commitShas) return false
        if (repositoryId != other.repositoryId) return false

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
        "Branch(id=$id, name='$name', active=$active, tracksFileRenames=$tracksFileRenames, latestCommit=$latestCommit, commitShas=$commitShas, repositoryId=$repositoryId)"
}
