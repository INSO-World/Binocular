package com.inso_world.binocular.model

import jakarta.validation.constraints.NotBlank
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Project — a named, top-level domain entity that may be associated with a [Repository].
 *
 * ### Identity & equality
 * - Technical identity: immutable [iid] of type [Id] (generated at construction).
 * - Business key: [uniqueKey] == validated [name].
 * - Equality is **identity-based** (same [iid]); `hashCode()` derives from [iid]. This intentionally
 *   overrides the default value-based semantics of a Kotlin `data class`.
 *
 * ### Construction & validation
 * - Requires a non-blank [name] (`@field:NotBlank` + runtime `require`).
 * - The constructor does **not** auto-wire repository relations; associate a repository via [repo] if needed.
 *
 * ### Relationships & mutability
 * - [repo] is optional and **set-once** (cannot be reassigned to a different repository; cannot be set to `null`).
 *
 * ### Thread-safety
 * - Instances are mutable and not thread-safe. Coordinate external synchronization for multi-step updates.
 *
 * @property name Human-readable project name; must be non-blank and forms the [uniqueKey].
 */
@OptIn(ExperimentalUuidApi::class)
data class Project(
    @field:NotBlank
    val name: String,
) : AbstractDomainObject<Project.Id, Project.Key>(
    Id(Uuid.random())
) {
    @JvmInline
    value class Id(val value: Uuid)

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(Repository::class.java)
    }

    data class Key(val name: String) // value object for lookups

    val accounts: MutableSet<Account> =
        object : NonRemovingMutableSet<Account>() {

            override fun add(element: Account): Boolean {
                // TODO require that project is in projects of account

                val added = super.add(element)
                return added
            }

            override fun addAll(elements: Collection<Account>): Boolean {
                // for bulk-adds make sure each one gets the same treatment
                var anyAdded = false
                for (e in elements) {
                    if (add(e)) anyAdded = true
                }
                return anyAdded
            }
        }

    val issues: MutableSet<Issue> = object : NonRemovingMutableSet<Issue>() {
        override fun add(element: Issue): Boolean {
            require(element.project == this@Project) {
                "Issue.project (${element.project}) doesn't match the project (${this@Project})."
            }
            val added = super.add(element)
            return added
        }

        override fun addAll(elements: Collection<Issue>): Boolean {
            var anyAdded = false
            for (element in elements) {
                if (add(element)) anyAdded = true
            }
            return anyAdded
        }
    }

    var description: String? = null

    /**
     * Optional owning [Repository].
     *
     * #### Semantics
     * - A project can exist without a repository.
     * - Assignment is **set-once** and **non-null**:
     *   - Reassigning the **same** instance is a no-op.
     *   - Reassigning to a **different** repository throws.
     *
     * #### Invariants enforced on set
     * - Precondition:
     *    - `value != null`
     *    - `this.repo == null || this.repo === value`
     *
     * #### Exceptions
     * - [IllegalArgumentException] if `value` is `null`.
     * - [IllegalArgumentException] if a different repository is assigned after one was already set.
     *
     * #### Thread-safety
     * - No internal synchronization; coordinate externally if multiple threads may mutate this property.
     */
    var repo: Repository? = null
        set(value) {
            requireNotNull(value) { "Cannot set repo to null" }
            if (value == this.repo) {
                return
            }
            if (this.repo != null) {
                throw IllegalArgumentException("Repository already set for Project $name: $repo")
            }
            field = value
        }

    // some database dependent id
    @Deprecated("Avoid using database specific id, use business key .iid", ReplaceWith("iid"))
    var id: String? = null

    init {
        require(name.isNotBlank())
    }

    override fun toString(): String = "Project(id=$id, iid=$iid, name='$name', description=$description)"

    override val uniqueKey: Project.Key
        get() = Project.Key(this.name)

    // Entities compare by immutable identity only
    override fun equals(other: Any?) = super.equals(other)
    override fun hashCode(): Int = super.hashCode()
}
