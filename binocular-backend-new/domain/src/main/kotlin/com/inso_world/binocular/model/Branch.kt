package com.inso_world.binocular.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Branch — a named pointer within a Git [Repository].
 *
 * ### Identity & equality
 * - Technical identity: immutable [iid] of type [Id] (assigned at construction).
 * - Business key: [uniqueKey] == [Key]([repository].iid, [name]).
 * - Equality delegates to [AbstractDomainObject] (identity-based); `hashCode()` derives from [iid].
 *
 * ### Construction & validation
 * - Requires a non-blank [name] (`@field:NotBlank` + runtime `require`).
 * - On initialization the instance registers itself in `repository.branches`
 *   (idempotent, add-only set).
 *
 * ### Relationships & collections
 * - [commits]: add-only, repository-consistent, bidirectionally maintained with `Commit.branches`.
 * - [files]: add-only collection keyed by business keys of [File]; exposed as `Set` for read-only use.
 *
 * ### Thread-safety
 * - The entity is mutable and not thread-safe. Collection fields use concurrent maps internally,
 *   but multi-step workflows are **not** atomic; coordinate externally.
 *
 * @property name Branch name; must be non-blank and participates in the [uniqueKey].
 * @property active Whether this branch is currently the active/checked-out branch.
 * @property tracksFileRenames Whether file rename tracking is enabled when analyzing history.
 * @property latestCommit Optional last known commit SHA associated with this branch.
 * @property repository Owning repository; this branch registers itself to `repository.branches` in `init`.
 */
@OptIn(ExperimentalUuidApi::class)
data class Branch(
    @field:NotBlank
    val name: String,
    val active: Boolean = false,
    val tracksFileRenames: Boolean = false,
    val latestCommit: String? = null,
    val repository: Repository,
) : AbstractDomainObject<Branch.Id, Branch.Key>(
    Id(Uuid.random())
) {
    @JvmInline
    value class Id(val value: Uuid)

    data class Key(val repositoryId: Repository.Id, val name: String)

    @Deprecated("Avoid using database specific id, use business key", ReplaceWith("iid"))
    var id: String? = null

    @Deprecated("legacy, use name property instead", replaceWith = ReplaceWith("name"))
    val branch: String = name

    val files: Set<File> =
        object : NonRemovingMutableSet<File>() {}

    init {
        require(name.isNotBlank()) { "name must not be blank" }
        repository.branches.add(this)
    }

    /**
     * The set of commits that belong to this [Branch].
     *
     * ### Semantics
     * - **Add-only collection:** Backed by [NonRemovingMutableSet] — any removal operation
     *   (`remove`, `retainAll`, `clear`, iterator `remove`) throws `UnsupportedOperationException`.
     * - **Repository consistency:** Every added [Commit] must belong to the **same** `repository`
     *   as this branch; otherwise the operation fails.
     * - **Bidirectional link:** On a successful insert, this branch is also added to
     *   `commit.branches`, keeping the relationship in sync.
     * - **Set semantics / de-duplication:** Membership is keyed by each commit’s `uniqueKey`
     *   (business key). Re-adding an already-present commit is a no-op (`false`).
     *
     * ### Validation
     * - Annotated with `@get:NotEmpty`: bean-validation frameworks (Jakarta Validation)
     *   will reject a [Branch] instance whose `commits` set is empty **at validation time**.
     *   Note that the set starts empty; ensure at least one commit is added before validation/persist.
     *
     * ### Invariants enforced on insert
     * - Precondition: `element.repository == this@Branch.repository`
     * - Postcondition (on success / no exception):
     *     - `element in commits`
     *     - `this@Branch in element.branches`
     *
     * ### Bulk adds
     * - `addAll` applies the same checks and back-linking as `add`, per element.
     * - Returns `true` if at least one new commit was added.
     * - **Not transactional:** if a later element fails (e.g., repo mismatch), previous
     *   successful inserts remain.
     *
     * ### Idempotency & recursion safety
     * - The back-link only runs when an element is newly added (`added == true`),
     *   preventing infinite mutual `add` calls between `branch.commits` and `commit.branches`.
     *
     * ### Exceptions
     * - Throws [IllegalArgumentException] if a commit from a different repository is added.
     *
     * ### Thread-safety
     * - Backed by a concurrent map internally, but multi-step workflows aren’t atomic;
     *   coordinate externally if mutated concurrently.
     */
    @get:NotEmpty
    val commits: MutableSet<Commit> =
        object : NonRemovingMutableSet<Commit>() {
            override fun add(element: Commit): Boolean {
                require(element.repository == this@Branch.repository) {
                    "Commit.repository (${element.repository}) doesn't match branch.repository (${this@Branch.repository})"
                }

                val added = super.add(element)
                if (added) {
                    // …and back-link to this as a child
                    element.branches.add(this@Branch)
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

    override val uniqueKey: Key
        get() = Key(repository.iid, this.name)

    // Entities compare by immutable identity only
    override fun equals(other: Any?) = super.equals(other)
    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String =
        "Branch(id=$id, iid=$iid, name='$name', active=$active, tracksFileRenames=$tracksFileRenames, latestCommit=$latestCommit, commitShas=${
            commits.map {
                it.sha
            }
        }, repositoryId=${repository.id})"
}
