package com.inso_world.binocular.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Domain entity representing a Git developer scoped to a [Repository].
 *
 * ## Identity & Equality
 * - Inherits entity identity from [Stakeholder] → [AbstractDomainObject].
 * - Technical id: [iid] of type [Id], generated at construction.
 * - Business key: [uniqueKey] = [Key]([repository].iid, [gitSignature]).
 * - Although this is a `data class`, `equals`/`hashCode` delegate to
 *   [AbstractDomainObject] (no value-based equality on properties).
 *
 * ## Construction
 * - Validates that [name] is non-blank.
 * - Validates that [email] is non-blank (required, unlike the old User model).
 * - Registers itself with `repository.developers` during `init` (idempotent, add-only collection).
 *
 * ## Relationships
 * - [committedCommits] and [authoredCommits] are add-only, bidirectionally maintained sets.
 * - [files] and [issues] are add-only collections keyed by domain/business keys.
 *
 * ## Migration from User
 * This class replaces the former `User` class with key differences:
 * - `email` is now **required** (was optional).
 * - Extends [Stakeholder] for better type hierarchy.
 * - Repository collection renamed from `user` to `developers`.
 *
 * ## Threading
 * - Instances are mutable and not thread-safe; coordinate external synchronization for multi-step updates.
 *
 * @property name Display name as used in Git signatures; must be non-blank.
 * @property email Email address as used in Git signatures; must be non-blank.
 * @property repository Owning repository; participates in the [uniqueKey] and scopes this developer.
 * @see Stakeholder
 * @see committedCommits
 * @see authoredCommits
 */
@OptIn(ExperimentalUuidApi::class)
data class Developer(
    @field:NotBlank
    override val name: String,
    @field:NotBlank
    override val email: String,
    @field:NotNull
    val repository: Repository,
) : Stakeholder<Developer.Id, Developer.Key>(
    Id(Uuid.random()),
) {
    /**
     * Business key for developer lookups within a repository.
     * Combines repository identity with git signature for uniqueness.
     */
    data class Key(val repositoryId: Repository.Id, val gitSignature: String)

    /**
     * Technical identifier for the developer entity.
     */
    @JvmInline
    value class Id(val value: Uuid)

    @Deprecated("Avoid using database specific id, use business key", ReplaceWith("iid"))
    var id: String? = null

    /**
     * Issues associated with this developer.
     */
    val issues: MutableSet<Issue> = mutableSetOf()

    /**
     * Files associated with this developer.
     */
    val files: MutableSet<File> = object : NonRemovingMutableSet<File>() {}

    init {
        require(name.trim().isNotBlank()) { "name cannot be blank." }
        require(email.trim().isNotBlank()) { "email cannot be blank." }
        repository.developers.add(this)
    }

    /**
     * Commits committed by this [Developer].
     *
     * # Semantics
     * - **Add-only collection:** Backed by `NonRemovingMutableSet` — removals are not supported.
     * - **Repository consistency:** Each added [Commit] must belong to the **same** `repository` as this developer.
     * - **Set semantics / de-duplication:** Membership is keyed by each commit's `uniqueKey` (business key).
     *   Re-adding an existing commit is a no-op (`false`).
     *
     * # Invariants enforced on insert
     * - Precondition: `element.repository == this@Developer.repository`.
     * - Precondition: `element.committer == this@Developer`.
     *
     * # Exceptions
     * - Throws [IllegalArgumentException] when the commit's repository differs from this developer's repository.
     * - Throws [IllegalArgumentException] when this developer is not the committer of the commit.
     * - Any attempt to remove elements from this collection throws [UnsupportedOperationException].
     *
     * # Thread-safety
     * - Internally backed by a concurrent map; individual `add`/`contains` operations are safe for concurrent use.
     */
    val committedCommits: MutableSet<Commit> = object : NonRemovingMutableSet<Commit>() {
        override fun add(element: Commit): Boolean {
            require(element.repository == this@Developer.repository) {
                "Commit.repository (${element.repository}) doesn't match developer.repository (${this@Developer.repository})"
            }
            require(element.committer == this@Developer) {
                "Cannot add Commit $element to committedCommits since developer is not committer of Commit."
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

    /**
     * Commits authored by this [Developer].
     *
     * # Semantics
     * - **Add-only collection:** Backed by `NonRemovingMutableSet` — removals are not supported.
     * - **Repository consistency:** Each added [Commit] must belong to the **same** `repository` as this developer.
     * - **Set semantics / de-duplication:** Membership is keyed by each commit's `uniqueKey` (business key).
     *   Re-adding an existing commit is a no-op (`false`).
     *
     * # Invariants enforced on insert
     * - Precondition: `element.repository == this@Developer.repository`.
     * - Precondition: `element.author == this@Developer`.
     *
     * # Exceptions
     * - Throws [IllegalArgumentException] when the commit's repository differs from this developer's repository.
     * - Throws [IllegalArgumentException] when this developer is not the author of the commit.
     * - Any attempt to remove elements from this collection throws [UnsupportedOperationException].
     *
     * # Thread-safety
     * - Internally backed by a concurrent map; individual `add`/`contains` operations are safe for concurrent use.
     */
    val authoredCommits: MutableSet<Commit> = object : NonRemovingMutableSet<Commit>() {
        override fun add(element: Commit): Boolean {
            require(element.repository == this@Developer.repository) {
                "Commit.repository (${element.repository}) doesn't match developer.repository (${this@Developer.repository})"
            }
            require(element.author == this@Developer) {
                "Cannot add Commit $element to authoredCommits since developer is not author of Commit."
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

    /**
     * Git signature format combining name and email.
     * Format: "Name <email@example.com>"
     */
    val gitSignature: String
        get() = "${name.trim()} <${email.trim()}>"

    override val uniqueKey: Key
        get() = Key(repository.iid, gitSignature)

    // Entities compare by immutable identity only
    override fun equals(other: Any?) = super.equals(other)
    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String =
        "Developer(id=$id, iid=$iid, name=$name, email=$email, gitSignature=$gitSignature, repositoryId=${repository.id})"
}
