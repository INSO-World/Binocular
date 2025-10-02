package com.inso_world.binocular.model

import com.inso_world.binocular.model.validation.NoCommitCycle
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import java.util.Objects
import java.util.concurrent.ConcurrentHashMap

/**
 * Domain model for a Commit, representing a commit in a Git repository.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
@NoCommitCycle
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
    @field:NotNull
    var repository: Repository? = null,
    val webUrl: String? = null,
    @Deprecated("do not use")
    val branch: String? = null,
    val stats: CommitDiff.Stats? = null,
    // Relationships
//    old stuff
    val builds: List<Build> = emptyList(),
    val files: List<File> = emptyList(),
    val modules: List<Module> = emptyList(),
    val issues: List<Issue> = emptyList(),
) : AbstractDomainObject(),
    Cloneable {
    // 1) private backing set
    private var _parents = mutableSetOf<Commit>()
    private var _children = mutableSetOf<Commit>()
    private val _branches = ConcurrentHashMap.newKeySet<Branch>()

    var committer: User? = null
        set(value) {
            if (value == this.committer) {
                return
            }
            if (this.committer != null) {
                throw IllegalArgumentException("Committer already set for Commit $sha: $committer")
            }
            field = value
            this.committer!!.committedCommits.add(this)
        }

    var author: User? = null
        set(value) {
            if (value == this.author) {
                return
            }
            if (this.author != null) {
                throw IllegalArgumentException("Author already set for Commit $sha: $author")
            }
            field = value
            this.author!!.authoredCommits.add(this)
        }

    var parents: MutableSet<Commit> =
        object : MutableSet<Commit> by _parents {
            override fun add(element: Commit): Boolean {
                // add to this commit’s parents…
                val added = _parents.add(element)
                if (added) {
                    // …and back-link to this as a child
                    element._children.add(this@Commit)
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

    var children: MutableSet<Commit> =
        object : MutableSet<Commit> by _children {
            override fun add(element: Commit): Boolean {
                // add to this commit’s parents…
                val added = _children.add(element)
                if (added) {
                    // …and back-link to this as a child
                    element._parents.add(this@Commit)
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

    val branches: MutableSet<Branch> =
        object : MutableSet<Branch> by _branches {
            override fun add(element: Branch): Boolean {
                // add to this commit’s parents…
                val added = _branches.add(element)
                if (added) {
                    // …and back-link to this as a child
                    element.commits.add(this@Commit)
                }
                return added
            }

            override fun addAll(elements: Collection<Branch>): Boolean {
                // for bulk‐adds make sure each one gets the same treatment
                var anyAdded = false
                for (e in elements) {
                    if (add(e)) anyAdded = true
                }
                return anyAdded
            }
        }

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
        if (javaClass != other?.javaClass) return false

        other as Commit

//        if (id != other.id) return false
        if (sha != other.sha) return false
//        if (authorDateTime != other.authorDateTime) return false
//        if (commitDateTime != other.commitDateTime) return false
//        if (message != other.message) return false
//        if (webUrl != other.webUrl) return false

        return true
    }

    override fun hashCode(): Int {
//        var result = Objects.hashCode(id)
//        result += 31 * Objects.hashCode(sha)
//        result += 31 * Objects.hashCode(authorDateTime)
//        result += 31 * Objects.hashCode(commitDateTime)
//        result += 31 * Objects.hashCode(message)
//        result += 31 * Objects.hashCode(webUrl)
        return Objects.hashCode(sha)
    }

    override fun toString(): String =
        "Commit(id=$id, sha='$sha', authorDateTime=$authorDateTime, commitDateTime=$commitDateTime, message=$message, webUrl=$webUrl, stats=$stats, author=${author?.name}, committer=${committer?.name}, repositoryId=${repository?.id})"

    override fun uniqueKey(): String = sha
}
