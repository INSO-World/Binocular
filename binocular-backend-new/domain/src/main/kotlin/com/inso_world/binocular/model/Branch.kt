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
    @field:NotNull
    var repository: Repository? = null,
) {

    companion object {
        /** Java friendly factory. */
        @JvmStatic
        fun create(
            name: String,
            repository: Repository,
            active: Boolean = false,
            tracksFileRenames: Boolean = false,
            latestCommit: String? = null,
        ): Branch =
            Branch(
                name = name,
                active = active,
                tracksFileRenames = tracksFileRenames,
                latestCommit = latestCommit,
                repository = repository,
            )
    }
    @Deprecated("legacy, use name property instead", replaceWith = ReplaceWith("name"))
    val branch: String = name

    @field:NotEmpty
    private val _commits: MutableSet<Commit> = mutableSetOf()

    @get:NotEmpty
    val commits: MutableSet<Commit> =
        object : MutableSet<Commit> by _commits {
            override fun add(element: Commit): Boolean {
                // add to this commit’s parents…
                val added = _commits.add(element)
                if (added) {
                    // …and back-link to this as a child
                    element.branches.add(this@Branch)
                }
                val parentsAdded = _commits.addAll(element.parents)
                return added || parentsAdded
            }

            override fun addAll(elements: Collection<Commit>): Boolean {
                // for bulk-adds make sure each one gets the same treatment
                var anyAdded = false
                for (e in elements) {
                    if (add(e)) anyAdded = true
                }
                return anyAdded
            }
        }

    fun uniqueKey(): String {
        val repo =
            requireNotNull(repository) {
                "Cannot generate unique key for $javaClass when repository is null"
            }
        return "${repo.localPath},$name"
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
        "Branch(id=$id, name='$name', active=$active, tracksFileRenames=$tracksFileRenames, latestCommit=$latestCommit, commitShas=${
            commits.map {
                it.sha
            }
        }, repositoryId=${repository?.id})"
}
