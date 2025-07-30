package com.inso_world.binocular.model

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.concurrent.ConcurrentHashMap

/**
 * Domain model for a User, representing a Git user.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
class User(
    var id: String? = null,
    @field:NotBlank val name: String? = null,
    @field:Email
    var email: String? = null,
    @field:NotNull
    var repository: Repository? = null,
    // Relationships
    val issues: List<Issue> = emptyList(),
    val files: List<File> = emptyList(),
) {
    private val _committedCommits = ConcurrentHashMap.newKeySet<Commit>()
    private val _authoredCommits = ConcurrentHashMap.newKeySet<Commit>()

    val committedCommits: MutableSet<Commit> =
        object : MutableSet<Commit> by _committedCommits {
            override fun add(element: Commit): Boolean {
                // add to this commit’s parents…
                val added = _committedCommits.add(element)
                if (added) {
                    // …and back-link to this as a child
                    element.committer = this@User
                }
                return added
            }

            override fun addAll(elements: Collection<Commit>): Boolean {
                // for bulk‐adds make sure each one gets the same treatment
                var anyAdded = false
                for (e in elements) {
                    if (add(e)) anyAdded = true
                }
                return anyAdded
            }
        }

    val authoredCommits: MutableSet<Commit> =
        object : MutableSet<Commit> by _authoredCommits {
            override fun add(element: Commit): Boolean {
                // add to this commit’s parents…
                val added = _authoredCommits.add(element)
                if (added) {
                    // …and back-link to this as a child
                    element.author = this@User
                }
                return added
            }

            override fun addAll(elements: Collection<Commit>): Boolean {
                // for bulk‐adds make sure each one gets the same treatment
                var anyAdded = false
                for (e in elements) {
                    if (add(e)) anyAdded = true
                }
                return anyAdded
            }
        }

    @Deprecated("do not use, just for compatibility")
    val gitSignature: String
        get() = "$name <$email>"

    fun uniqueKey(): String {
        if (repository == null) {
            throw IllegalStateException("Cannot generate unique key for $javaClass when repository is null")
        }
        return "${repository?.name},$email"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (name != other.name) return false
        if (repository?.name != other.repository?.name) return false
//        if (gitSignature != other.gitSignature) return false
//        if (authoredCommits != other.authoredCommits) return false
//        if (committedCommits != other.committedCommits) return false
//        if (issues != other.issues) return false
//        if (files != other.files) return false

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
