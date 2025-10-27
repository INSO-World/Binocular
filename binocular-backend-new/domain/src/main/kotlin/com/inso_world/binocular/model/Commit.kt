package com.inso_world.binocular.model

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private fun Char.isHex(): Boolean =
    this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'

/**
 * Commit — a Git snapshot that belongs to a [Repository].
 *
 * ## Identity & equality
 * - Technical identity: immutable [iid] of type [Id] (generated at construction).
 * - Business key: [uniqueKey] == [sha].
 * - Equality follows [AbstractDomainObject]: same runtime class **and** equal [iid] and [uniqueKey];
 *   `hashCode()` derives from [iid].
 *
 * ## Construction & validation
 * - [sha] must be exactly 40 hexadecimal characters (`[0-9a-fA-F]`).
 * - [commitDateTime] and [authorDateTime] (if present) must be past-or-present.
 * - On initialization the instance registers itself in `repository.commits`
 *   (idempotent, add-only set). Parent/child/branch links are **not** wired automatically.
 *
 * ## Relationships
 * - [author] / [committer]: set-once, non-null on assignment, repository-consistent; setting either
 *   also back-links this commit to the respective user collection.
 * - [parents], [children], [branches]: add-only, repository-consistent, bidirectionally maintained.
 *   See their individual KDoc for invariants and exceptions.
 *
 * ## Thread-safety
 * - The entity is mutable and not thread-safe. Collection fields use concurrent maps internally,
 *   but multi-step workflows are **not** atomic; coordinate externally.
 *
 * @property sha 40-character hex SHA-1 identifying the commit; forms the business key.
 * @property authorDateTime Logical author timestamp (optional); must be past-or-present when provided.
 * @property commitDateTime Commit timestamp; must be past-or-present.
 * @property message Optional commit message summary/body.
 * @property repository Owning repository; the commit registers itself to `repository.commits` in `init`.
 */
@OptIn(ExperimentalUuidApi::class)
data class Commit(
    @field:Size(min = 40, max = 40)
    val sha: String,
    @field:PastOrPresent
    val authorDateTime: LocalDateTime? = null,
    @field:PastOrPresent
    @field:NotNull
    val commitDateTime: LocalDateTime? = null,
    val message: String? = null,
    val repository: Repository,
) : AbstractDomainObject<Commit.Id, Commit.Key>(
    Id(Uuid.random())
) {
    @JvmInline
    value class Id(val value: Uuid)

    data class Key(val sha: String) // value object for lookups

    @Deprecated("Avoid using database specific id, use business key", ReplaceWith("iid"))
    var id: String? = null

    //    old stuff
    var webUrl: String? = null

    @Deprecated("do not use")
    var branch: String? = null
    var stats: Stats? = null
    val builds: List<Build> = emptyList()
    val files: List<File> = emptyList()
    val modules: List<Module> = emptyList()
    val issues: List<Issue> = emptyList()

    init {
        // add 1ns so PastOrPresent can be evaluated
        val initDt = LocalDateTime.now().plusNanos(1)
        require(sha.length == 40) { "SHA must be 40 hex chars, got ${sha.length}" }
        require(sha.all { it.isHex() }) { "SHA-1 must be hex [0-9a-fA-F]" }
        require(commitDateTime?.isBefore(initDt) == true) {
            "commitDateTime ($commitDateTime) must be past or present ($initDt)"
        }
        require(authorDateTime?.isBefore(initDt) == true) {
            "authorDateTime ($authorDateTime) must be past or present ($initDt)"
        }
        this.repository.commits.add(this)
    }

    /**
     * The committer of this [Commit].
     *
     * ### Semantics
     * - **Set-once, non-null:** `committer` must never be set to `null`. Once assigned,
     *   it cannot be changed to a different user. Re-assigning the *same* user (by `equals`)
     *   is treated as a no-op.
     * - **Repository consistency:** The assigned user's `repository` must be the same
     *   as this commit's `repository`.
     * - **Bidirectional link:** On successful assignment, this commit is added to
     *   `committer.committedCommits`.
     *
     * ### Invariants enforced by the setter
     * - Precondition:
     *     - `value != null`
     *     - `this.committer == null || this.committer == value`
     *     - `value.repository == this.repository`
     *
     * ### Exceptions
     * - Throws [IllegalArgumentException] if:
     *   - A `null` value is provided.
     *   - A different committer is assigned after one was already set.
     *   - The user's repository differs from the commit's repository.
     *
     * ### Idempotency
     * - If `value == this.committer` (according to `equals`), the call returns early
     *   without side effects. This allows re-hydrating the same identity without churn.
     *
     * ### Thread-safety
     * - No synchronization is performed; external coordination is required if this
     *   entity can be mutated concurrently.
     */
    var committer: User? = null
        set(value) {
            requireNotNull(value) { "committer cannot be set to null" }
            if (value == this.committer) {
                return
            }
            if (this.committer != null) {
                throw IllegalArgumentException("committer already set for Commit $sha: $committer")
            }
            if (value.repository != this.repository) {
                throw IllegalArgumentException("Repository between $value and Commit do not match: ${this.repository}")
            }
            field = value
            value.committedCommits.add(this)
        }

    /**
     * The author of this [Commit].
     *
     * ### Semantics
     * - **Set-once, non-null:** `author` must never be set to `null`. Once assigned,
     *   it cannot be changed to a different user. Re-assigning the *same* user (by `equals`)
     *   is a no-op.
     * - **Repository consistency:** The assigned user's `repository` must equal this
     *   commit's `repository`.
     * - **Bidirectional link:** On successful assignment, this commit is added to
     *   `author.authoredCommits`.
     *
     * ### Invariants enforced by the setter
     * - `value != null`
     * - `this.author == null || this.author == value`
     * - `value.repository == this.repository`
     *
     * ### Exceptions
     * - Throws [IllegalArgumentException] if:
     *   - A `null` value is provided.
     *   - A different author is assigned after one was already set.
     *   - The user's repository differs from the commit's repository.
     *
     * ### Idempotency
     * - If `value == this.author`, the call returns early without side effects.
     *
     * ### Thread-safety
     * - No synchronization is performed; external coordination is required if this
     *   entity can be mutated concurrently.
     */
    var author: User? = null
        set(value) {
            requireNotNull(value) { "author cannot be set to null" }
            if (value == this.author) {
                return
            }
            if (this.author != null) {
                throw IllegalArgumentException("Author already set for Commit $sha: $author")
            }
            if (value.repository != this.repository) {
                throw IllegalArgumentException("Repository between $value and Commit do not match: ${this.repository}")
            }
            field = value
            value.authoredCommits.add(this)
        }

    /**
     * Direct parent commits of this [Commit].
     *
     * ### Semantics
     * - **Non-removable set:** Backed by [NonRemovingMutableSet] — removals are disallowed.
     *   Once a parent is added, it cannot be removed via this API.
     * - **Repository consistency:** Every parent must belong to the same `repository`
     *   as this commit; cross-repository ancestry is rejected.
     * - **Bidirectional link:** On successful `add`, this commit is *also* added to
     *   the parent’s `children` set to keep the relationship consistent.
     * - **Set semantics:** Duplicates (by `equals`/`hashCode`) are ignored; re-adding
     *   an existing parent is a no-op and returns `false`.
     *
     * ### Invariants enforced on insert
     * - Precondition: `element.repository == this.repository`
     * - Postcondition (on success / no exception):
     *     - `element in parents`
     *     - `this in element.children`
     *
     * ### Bulk adds
     * - `addAll` applies the same per-element checks and back-link behavior as `add`.
     * - Returns `true` if at least one new parent was added.
     * - **Not transactional:** if a later element fails (e.g., repository mismatch),
     *   earlier successful inserts remain in effect.
     *
     * ### Exceptions
     * - Throws [IllegalArgumentException] if a parent from a different repository is added.
     *
     * ### Thread-safety
     * - Backed by a concurrent map internally, but multi-step workflows aren’t atomic;
     *   coordinate externally if mutated concurrently.
     */
    val parents: MutableSet<Commit> =
        object : NonRemovingMutableSet<Commit>() {
            override fun add(element: Commit): Boolean {
                if (element.repository != this@Commit.repository) {
                    throw IllegalArgumentException("Repository between $element and Commit do not match: ${this@Commit.repository}")
                }
                // add to this commit’s parents…
                val added = super.add(element)
                if (added) {
                    // …and back-link to this as a child
                    element.children.add(this@Commit)
                }
                return added
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

    /**
     * Direct child commits of this [Commit].
     *
     * ### Semantics
     * - **Non-removable set:** Backed by [NonRemovingMutableSet] — removals are disallowed.
     *   Once a child is added, it cannot be removed via this API.
     * - **Repository consistency:** Every child must belong to the same `repository`
     *   as this commit; cross-repository relationships are rejected.
     * - **Bidirectional link:** On successful `add`, this commit is *also* added to
     *   the child’s `parents` set to keep the relationship consistent.
     * - **Set semantics:** Duplicates (by `equals`/`hashCode`) are ignored; re-adding
     *   an existing child is a no-op and returns `false`.
     *
     * ### Invariants enforced on insert
     * - Precondition: `element.repository == this.repository`
     * - Postcondition (on success / no exception):
     *     - `element in children`
     *     - `this in element.parents`
     *
     * ### Bulk adds
     * - `addAll` applies the same per-element checks and back-link behavior as `add`.
     * - Returns `true` if at least one new child was added.
     * - **Not transactional:** if a later element fails (e.g., repository mismatch),
     *   earlier successful inserts remain in effect.
     *
     * ### Exceptions
     * - Throws [IllegalArgumentException] if a child from a different repository is added.
     *
     * ### Thread-safety
     * - Backed by a concurrent map internally, but multi-step workflows aren’t atomic;
     *   coordinate externally if mutated concurrently.
     */
    val children: MutableSet<Commit> =
        object : NonRemovingMutableSet<Commit>() {
            override fun add(element: Commit): Boolean {
                if (element.repository != this@Commit.repository) {
                    throw IllegalArgumentException("Repository between $element and Commit do not match: ${this@Commit.repository}")
                }
                // add to this commit’s parents…
                val added = super.add(element)
                if (added) {
                    // …and back-link to this as a child
                    element.parents.add(this@Commit)
                }
                return added
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

    /**
     * The set of [Branch]es that reference this [Commit].
     *
     * ### Semantics
     * - **Add-only collection:** Backed by [NonRemovingMutableSet] — any removal operation
     *   (`remove`, `retainAll`, `clear`, iterator `remove`) throws `UnsupportedOperationException`.
     * - **Repository consistency:** Each added branch must belong to the **same** `repository`
     *   as this commit; otherwise the operation fails.
     * - **Bidirectional link:** On successful insert, this commit is also added to
     *   `branch.commits`, keeping the association in sync.
     * - **Set semantics / de-duplication:** Membership is keyed by each branch’s `uniqueKey`
     *   (business key). Re-adding an existing branch is a no-op (`false`).
     *
     * ### Validation
     * - Annotated with `@get:NotEmpty`: bean-validation frameworks (Jakarta Validation)
     *   will reject a [Commit] instance whose `branches` set is empty **at validation time**.
     *   Note that the set starts empty; ensure at least one commit is added before validation/persist.
     *
     * ### Invariants enforced on insert
     * - Precondition: `element.repository == this@Commit.repository`
     * - Postcondition (on success / no exception):
     *     - `element in branches`
     *     - `this@Commit in element.commits`
     *
     * ### Bulk adds
     * - `addAll` applies the same checks and back-linking as `add`, per element.
     * - Returns `true` if at least one new branch was added.
     * - **Not transactional:** if a later element fails (e.g., repo mismatch), prior
     *   successful inserts remain.
     *
     * ### Idempotency & recursion safety
     * - The back-link only runs when an element is newly added (`added == true`),
     *   preventing infinite mutual `add` calls between `commit.branches` and `branch.commits`.
     *
     * ### Exceptions
     * - Throws [IllegalArgumentException] if a branch from a different repository is added.
     *
     * ### Thread-safety
     * - Internally uses a concurrent map for storage, but multi-step workflows aren’t atomic;
     *   coordinate externally if mutated concurrently.
     */
    @get:NotEmpty
    val branches: MutableSet<Branch> =
        object : NonRemovingMutableSet<Branch>() {
            override fun add(element: Branch): Boolean {
                require(element.repository == this@Commit.repository) {
                    "Branch.repository (${element.repository}) doesn't match commit.repository (${this@Commit.repository})"
                }

                val added = super.add(element)
                if (added) {
                    // …and back-link to this as a child
                    element.commits.add(this@Commit)
                }
                return added
            }

            override fun addAll(elements: Collection<Branch>): Boolean {
                // for bulk-adds make sure each one gets the same treatment
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
    override val uniqueKey: Key
        get() = Commit.Key(sha)

    // Entities compare by immutable identity only
    override fun equals(other: Any?) = super.equals(other)
    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String =
        "Commit(id=$id, sha='$sha', authorDateTime=$authorDateTime, commitDateTime=$commitDateTime, message=$message, webUrl=$webUrl, stats=$stats, author=$author, committer=$committer, repositoryId=${repository?.id}, children=${children.map { it.sha }}, parents=${parents.map { it.sha }}, branches=${branches.map { it.toString() }})"
}
