package com.inso_world.binocular.model

import com.inso_world.binocular.model.validation.Hexadecimal
import com.inso_world.binocular.model.validation.isHex
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Commit — a Git snapshot that belongs to a [Repository].
 *
 * ## Identity & Equality
 * - Technical identity: immutable [iid] of type [Id] (generated at construction).
 * - Business key: [uniqueKey] == [sha].
 * - Equality follows [AbstractDomainObject]: same runtime class **and** equal [iid] and [uniqueKey];
 *   `hashCode()` derives from [iid].
 *
 * ## Construction & Validation
 * - [sha] must be exactly 40 hexadecimal characters (`[0-9a-fA-F]`).
 * - [authorSignature] is **required** and contains the author [Developer] and timestamp.
 * - [committerSignature] is **optional**; if not provided, defaults to [authorSignature].
 * - Both signatures' timestamps must be past-or-present.
 * - Both signatures' developers must belong to the same repository as this commit.
 * - On initialization the instance registers itself in `repository.commits`,
 *   `author.authoredCommits`, and `committer.committedCommits`.
 *
 * ## Git Semantics
 * - **Author**: The person who originally wrote the code (captured in [authorSignature]).
 * - **Committer**: The person who committed the code (captured in [committerSignature]).
 * - These can differ when patches are applied, commits are cherry-picked, or rebased.
 * - When the same person authors and commits, [committerSignature] can be omitted (defaults to author).
 *
 * ## Relationships
 * - [author]: Derived from [authorSignature.developer][Signature.developer].
 * - [committer]: Derived from [committerSignature.developer][Signature.developer].
 * - [parents], [children]: Add-only, repository-consistent, bidirectionally maintained.
 *
 * ## Thread-safety
 * - The entity is mutable and not thread-safe. Collection fields use concurrent maps internally,
 *   but multi-step workflows are **not** atomic; coordinate externally.
 *
 * @property sha 40-character hex SHA-1 identifying the commit; forms the business key.
 * @property authorSignature The signature of the commit's author (required).
 * @property committerSignature The signature of the committer (optional, defaults to author).
 * @property message Optional commit message summary/body.
 * @property repository Owning repository; the commit registers itself to `repository.commits` in `init`.
 * @see Signature
 * @see Developer
 */
@OptIn(ExperimentalUuidApi::class)
data class Commit(
    @field:Size(min = 40, max = 40)
    @field:Hexadecimal
    val sha: String,
    @field:NotNull
    @field:Valid
    val authorSignature: Signature,
    @field:Valid
    @field:NotNull
    val committerSignature: Signature = authorSignature,
    val message: String? = null,
    @field:NotNull
    val repository: Repository,
) : AbstractDomainObject<Commit.Id, Commit.Key>(
    Id(Uuid.random())
) {
    @JvmInline
    value class Id(val value: Uuid)

    data class Key(val sha: String)

    @Deprecated("Avoid using database specific id, use business key", ReplaceWith("iid"))
    var id: String? = null

    var webUrl: String? = null

    @Deprecated("do not use")
    var branch: String? = null
    var stats: Stats? = null
    val builds: List<Build> = emptyList()
    val files: List<File> = emptyList()
    val modules: List<Module> = emptyList()
    val issues: List<Issue> = emptyList()

    /**
     * The author [Developer] of this commit.
     * Derived from [authorSignature].
     */
    val author: Developer
        get() = authorSignature.developer

    /**
     * The committer [Developer] of this commit.
     * Derived from [committerSignature], defaults to [author] if not explicitly set.
     */
    val committer: Developer
        get() = committerSignature.developer

    /**
     * The author timestamp.
     * Derived from [authorSignature.timestamp][Signature.timestamp].
     */
    val authorDateTime: LocalDateTime
        get() = authorSignature.timestamp

    /**
     * The commit timestamp.
     * Derived from [committerSignature.timestamp][Signature.timestamp],
     * defaults to [authorSignature.timestamp] if [committerSignature] was not explicitly set.
     */
    val commitDateTime: LocalDateTime
        get() = committerSignature.timestamp

    init {
        require(sha.length == 40) { "SHA must be 40 hex chars, got ${sha.length}" }
        require(sha.all { it.isHex() }) { "SHA-1 must be hex [0-9a-fA-F]" }
        require(authorSignature.developer.repository == this.repository) {
            "Repository between author ${authorSignature.developer} and Commit do not match: ${this.repository}"
        }
        require(committerSignature.developer.repository == this.repository) {
            "Repository between committer ${committerSignature.developer} and Commit do not match: ${this.repository}"
        }

        // Register commit to repository and developers
        this.repository.commits.add(this)
        author.authoredCommits.add(this)
        committer.committedCommits.add(this)
    }

    /**
     * Direct parent commits of this [Commit].
     *
     * ### Semantics
     * - **Non-removable set:** Backed by [NonRemovingMutableSet] — removals are disallowed.
     * - **Repository consistency:** Every parent must belong to the same `repository`.
     * - **Bidirectional link:** On successful `add`, this commit is added to the parent's `children`.
     * - **Set semantics:** Duplicates are ignored; re-adding is a no-op.
     *
     * ### Exceptions
     * - Throws [IllegalArgumentException] if a parent from a different repository is added.
     * - Throws [IllegalArgumentException] if adding self as parent.
     * - Throws [IllegalArgumentException] if the element is already in children.
     */
    val parents: MutableSet<Commit> =
        object : NonRemovingMutableSet<Commit>() {
            override fun add(element: Commit): Boolean {
                require(element.repository == this@Commit.repository) {
                    "Repository between $element and Commit do not match: ${this@Commit.repository}"
                }
                require(element != this@Commit) {
                    "Commit cannot be its own parent"
                }
                require(!this@Commit.children.contains(element)) {
                    "${element.sha} is already present in '${this@Commit.sha}' children collection. Cannot be added as parent too."
                }
                val added = super.add(element)
                if (added) {
                    element.children.add(this@Commit)
                }
                return added
            }

            override fun addAll(elements: Collection<Commit>): Boolean {
                var anyAdded = false
                for (e in elements) {
                    if (add(e)) anyAdded = true
                }
                return anyAdded
            }
        }

    /**
     * Direct child commits of this [Commit].
     *
     * ### Semantics
     * - **Non-removable set:** Backed by [NonRemovingMutableSet] — removals are disallowed.
     * - **Repository consistency:** Every child must belong to the same `repository`.
     * - **Bidirectional link:** On successful `add`, this commit is added to the child's `parents`.
     * - **Set semantics:** Duplicates are ignored; re-adding is a no-op.
     *
     * ### Exceptions
     * - Throws [IllegalArgumentException] if a child from a different repository is added.
     * - Throws [IllegalArgumentException] if adding self as child.
     * - Throws [IllegalArgumentException] if the element is already in parents.
     */
    val children: MutableSet<Commit> =
        object : NonRemovingMutableSet<Commit>() {
            override fun add(element: Commit): Boolean {
                require(element.repository == this@Commit.repository) {
                    "Repository between $element and Commit do not match: ${this@Commit.repository}"
                }
                require(element != this@Commit) {
                    "Commit cannot be its own child"
                }
                require(!this@Commit.parents.contains(element)) {
                    "${element.sha} is already present in '${this@Commit.sha}' parent collection. Cannot be added as child too."
                }
                val added = super.add(element)
                if (added) {
                    element.parents.add(this@Commit)
                }
                return added
            }

            override fun addAll(elements: Collection<Commit>): Boolean {
                var anyAdded = false
                for (e in elements) {
                    if (add(e)) anyAdded = true
                }
                return anyAdded
            }
        }

    @Deprecated("Do not use")
    val users: List<Developer>
        get() = listOfNotNull(author, committer.takeIf { it != author })

    override val uniqueKey: Key
        get() = Key(sha)

    // Entities compare by immutable identity only
    override fun equals(other: Any?) = super.equals(other)
    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String =
        "Commit(id=$id, sha='$sha', authorDateTime=$authorDateTime, commitDateTime=$commitDateTime, message=$message, webUrl=$webUrl, stats=$stats, author=$author, committer=$committer, repositoryId=${repository.id}, children=${children.map { it.sha }}, parents=${parents.map { it.sha }})"
}
