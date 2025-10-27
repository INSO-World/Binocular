package com.inso_world.binocular.model

import jakarta.validation.constraints.NotBlank
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Domain entity representing a Git user scoped to a [Repository].
 *
 * ## Identity & equality
 * - Inherits entity identity from [AbstractDomainObject].
 *   - Technical id: [iid] of type [Id], generated at construction.
 *   - Business key: [uniqueKey] = [User.Key]([repository].iid, [name].trim()).
 * - Although this is a `data class`, `equals`/`hashCode` delegate to
 *   [AbstractDomainObject] (no value-based equality on properties).
 *
 * ## Construction
 * - Validates that [name] is non-blank.
 * - Registers itself with `repository.user` during `init` (idempotent, add-only collection).
 *
 * ## Relationships
 * - [committedCommits] and [authoredCommits] are add-only, bidirectionally maintained sets.
 * - [files] and [issues] are add-only collections keyed by domain/business keys.
 *
 * ## Validation
 * - [name] is annotated with `@NotBlank`.
 * - [email] (optional) rejects blank values when set.
 *
 * ## Threading
 * - Instances are mutable and not thread-safe; coordinate external synchronization for multi-step updates.
 *
 * @property name Display name as used in Git signatures; must be non-blank.
 * @property repository Owning repository; participates in the [uniqueKey] and scopes this user.
 * @see committedCommits
 * @see authoredCommits
 */
@OptIn(ExperimentalUuidApi::class)
data class User(
    @field:NotBlank val name: String,
    val repository: Repository,
) : AbstractDomainObject<User.Id, User.Key>(
    Id(Uuid.random()),
) {
    data class Key(val repositoryId: Repository.Id, val name: String) // value object for lookups

    @JvmInline
    value class Id(val value: Uuid)

    @Deprecated("Avoid using database specific id, use business key", ReplaceWith("iid"))
    var id: String? = null

    var email: String? = null
        set(value) {
            require(value?.trim()?.isNotBlank() == true) { "Email must not be empty" }
            field = value
        }

    // Relationships
    val issues: MutableSet<Issue> = mutableSetOf()

    //        object : NonRemovingMutableSet<Issue>() {}
    val files: MutableSet<File> = object : NonRemovingMutableSet<File>() {}

    init {
        require(name.trim().isNotBlank()) { "name cannot be blank." }
        repository.user.add(this)
    }

    /**
     * Commits committed by this [User].
     *
     * # Semantics
     * - **Add-only collection:** Backed by `NonRemovingMutableSet` — removals (`remove`, `retainAll`, `clear`,
     *   iterator `remove`) are not supported.
     * - **Repository consistency:** Each added [Commit] must belong to the **same** `repository` as this user.
     * - **Bidirectional link:** On successful insert, this user is assigned as the commit’s `committer`
     *   (`element.committer = this@User`), keeping both sides in sync.
     * - **Set semantics / de-duplication:** Membership is keyed by each commit’s `uniqueKey` (business key).
     *   Re-adding an existing commit is a no-op (`false`).
     *
     * # Invariants enforced on insert
     * - Precondition: `element.repository == this@User.repository`.
     * - Postcondition (on success / no exception):
     *     - `element in committedCommits`
     *     - `element.committer == this@User`
     *
     * # Bulk adds
     * - `addAll` applies the same checks and back-linking as `add`, per element.
     * - Returns `true` iff at least one new commit was added.
     * - **Not transactional:** if a later element fails (e.g., repository mismatch or `author` already set
     *   to a different user), earlier successful inserts remain. Callers should handle rollback if needed.
     *
     * # Idempotency & recursion safety
     * - The back-link (`element.committer = this@User`) runs **only** when the commit is newly added
     *   (`added == true`), preventing infinite mutual updates.
     * - Re-adding the same commit (by `uniqueKey`) is a no-op and does not re-trigger the back-link.
     *
     * # Exceptions
     * - Throws [IllegalArgumentException] when the commit’s repository differs from this user’s repository.
     * - May throw [IllegalArgumentException] from `Commit.committer`’s setter if the commit already has a
     *   different committer assigned (set-once constraint).
     * - Any attempt to remove elements from this collection throws [UnsupportedOperationException].
     *
     * # Thread-safety
     * - Internally backed by a concurrent map; individual `add`/`contains` operations are safe for concurrent use.
     *   However, multi-step workflows (e.g., “add then do X”) are **not atomic**; coordinate externally to
     *   avoid torn updates between set membership and the `author` back-link.
     */
    val committedCommits: MutableSet<Commit> = object : NonRemovingMutableSet<Commit>() {
        override fun add(element: Commit): Boolean {
            require(element.repository == this@User.repository) {
                "Commit.repository (${element.repository}) doesn't match user.repository (${this@User.repository})"
            }
            val added = super.add(element)
            if (added) {
                // …and back-link to this as a child
                element.committer = this@User
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
     * Commits authored by this [User].
     *
     * # Semantics
     * - **Add-only collection:** Backed by `NonRemovingMutableSet` — removals (`remove`, `retainAll`, `clear`,
     *   iterator `remove`) are not supported.
     * - **Repository consistency:** Each added [Commit] must belong to the **same** `repository` as this user.
     * - **Bidirectional link:** On successful insert, this user is assigned as the commit’s `author`
     *   (`element.author = this@User`), keeping both sides in sync.
     * - **Set semantics / de-duplication:** Membership is keyed by each commit’s `uniqueKey` (business key).
     *   Re-adding an existing commit is a no-op (`false`).
     *
     * # Invariants enforced on insert
     * - Precondition: `element.repository == this@User.repository`.
     * - Postcondition (on success / no exception):
     *     - `element in authoredCommits`
     *     - `element.author == this@User`
     *
     * # Bulk adds
     * - `addAll` applies the same checks and back-linking as `add`, per element.
     * - Returns `true` iff at least one new commit was added.
     * - **Not transactional:** if a later element fails (e.g., repository mismatch or `author` already set
     *   to a different user), earlier successful inserts remain. Callers should handle rollback if needed.
     *
     * # Idempotency & recursion safety
     * - The back-link (`element.author = this@User`) runs **only** when the commit is newly added
     *   (`added == true`), preventing infinite mutual updates.
     * - Re-adding the same commit (by `uniqueKey`) is a no-op and does not re-trigger the back-link.
     *
     * # Exceptions
     * - Throws [IllegalArgumentException] when the commit’s repository differs from this user’s repository.
     * - May throw [IllegalArgumentException] from `Commit.author`’s setter if the commit already has a
     *   different author assigned (set-once constraint).
     * - Any attempt to remove elements from this collection throws [UnsupportedOperationException].
     *
     * # Thread-safety
     * - Internally backed by a concurrent map; individual `add`/`contains` operations are safe for concurrent use.
     *   However, multi-step workflows (e.g., “add then do X”) are **not atomic**; coordinate externally to
     *   avoid torn updates between set membership and the `author` back-link.
     */
    val authoredCommits: MutableSet<Commit> = object : NonRemovingMutableSet<Commit>() {
        override fun add(element: Commit): Boolean {
            require(element.repository == this@User.repository) {
                "Commit.repository (${element.repository}) doesn't match user.repository (${this@User.repository})"
            }

            val added = super.add(element)
            if (added) {
                // …and back-link to this as a child
                element.author = this@User
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

    @Deprecated("do not use, just for compatibility")
    val gitSignature: String
        get() = "$name <$email>"

    override val uniqueKey: Key
        get() = Key(repository.iid, name.trim())

    // Entities compare by immutable identity only
    override fun equals(other: Any?) = super.equals(other)
    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String =
        "User(id=$id, iid=$iid, name=$name, gitSignature=$gitSignature, repositoryId=${repository.id}, committedCommits=${committedCommits.map { it.sha }}, authoredCommits=${authoredCommits.map { it.sha }})"
}
