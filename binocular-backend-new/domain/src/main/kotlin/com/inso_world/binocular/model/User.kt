package com.inso_world.binocular.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Domain entity representing a Git user scoped to a [Repository].
 *
 * @deprecated Use [Developer] instead. This class is maintained for backwards compatibility only.
 * The new [Developer] class provides better semantics with required email and proper [Signature] integration.
 *
 * ## Migration Guide
 * - Replace `User` with `Developer`
 * - `email` is now required in `Developer` (was optional in `User`)
 * - Use [Signature] for commit author/committer timestamps
 * - Use `repository.developers` instead of `repository.user`
 *
 * ## Identity & equality
 * - Inherits entity identity from [AbstractDomainObject].
 *   - Technical id: [iid] of type [Id], generated at construction.
 *   - Business key: [uniqueKey] = [User.Key]([repository].iid, [name].trim()).
 * - Although this is a `data class`, `equals`/`hashCode` delegate to
 *   [AbstractDomainObject] (no value-based equality on properties).
 *
 * @property name Display name as used in Git signatures; must be non-blank.
 * @property repository Owning repository; participates in the [uniqueKey] and scopes this user.
 * @see Developer
 * @see Signature
 */
@Deprecated("Use Developer instead", ReplaceWith("Developer"))
@OptIn(ExperimentalUuidApi::class)
data class User(
    @field:NotBlank val name: String,
    @field:NotNull val repository: Repository,
) : AbstractDomainObject<User.Id, User.Key>(
    Id(Uuid.random()),
) {
    data class Key(val repositoryId: Repository.Id, val gitSignature: String) // value object for lookups

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
            require(element.committer == this@User) {
                "Cannot add Commit $element to committedCommits since user is not committer of Commit."
            }
            val added = super.add(element)
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
     * @deprecated This collection is deprecated along with [User]. Use [Developer.authoredCommits] instead.
     * Note: This collection no longer back-links to commits as the new [Commit] model uses [Signature].
     */
    @Deprecated("Use Developer.authoredCommits instead")
    val authoredCommits: MutableSet<Commit> = object : NonRemovingMutableSet<Commit>() {
        override fun add(element: Commit): Boolean {
            require(element.repository == this@User.repository) {
                "Commit.repository (${element.repository}) doesn't match user.repository (${this@User.repository})"
            }
            return super.add(element)
        }

        override fun addAll(elements: Collection<Commit>): Boolean {
            var anyAdded = false
            for (e in elements) {
                if (add(e)) anyAdded = true
            }
            return anyAdded
        }
    }

    val gitSignature: String
        get() = "${name.trim()} <${email?.trim()}>"

    override val uniqueKey: Key
        get() = Key(repository.iid, gitSignature)

    // Entities compare by immutable identity only
    override fun equals(other: Any?) = super.equals(other)
    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String =
        "User(id=$id, iid=$iid, name=$name, gitSignature=$gitSignature, repositoryId=${repository.id}, committedCommits=${committedCommits.map { it.sha }}, authoredCommits=${authoredCommits.map { it.sha }})"
}
