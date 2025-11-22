package com.inso_world.binocular.model

import com.inso_world.binocular.model.vcs.Remote
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Repository — domain entity representing a local Git repository scoped to a [Project].
 *
 * ### Identity & equality
 * - Technical identity: immutable [iid] of type [Id] (generated at construction).
 * - Business key: [uniqueKey] == [Key]([project].iid, [localPath].trim()).
 * - Equality is identity-based (same [iid]); `hashCode()` derives from [iid].
 *
 * ### Construction & validation
 * - Requires a non-blank [localPath] (`@field:NotBlank` + runtime `require`).
 * - On construction, the repository **links itself** to the owning [project] via `project.repo = this`.
 *
 * ### Relationships & collections
 * - [commits], [branches], [user], and [remotes] are add-only, repository-consistent, de-duplicated sets backed by
 *   `NonRemovingMutableSet`. See their KDoc for invariants and exceptions.
 *
 * ### Thread-safety
 * - Instances are mutable and not thread-safe. Collections use concurrent maps for element-level ops,
 *   but multi-step workflows are **not atomic**; coordinate externally.
 *
 * @property localPath Absolute or workspace-relative path to the repository; must be non-blank.
 *   Participates in [uniqueKey] as `localPath.trim()`.
 * @property project Owning [Project]; establishes the [Repository]↔[Project] association during `init`.
 */
@OptIn(ExperimentalUuidApi::class)
data class Repository(
    @field:NotBlank
    @field:Size(max = 255)
    val localPath: String,
    val project: Project,
) : AbstractDomainObject<Repository.Id, Repository.Key>(
    Id(Uuid.random())
) {
    @JvmInline
    value class Id(val value: Uuid)

    data class Key(val projectId: Project.Id, val localPath: String) // value object for lookups

    // some database dependent id
    @Deprecated("Avoid using database specific id, use business key .iid", ReplaceWith("iid"))
    var id: String? = null

    init {
        require(localPath.trim().isNotBlank()) { "localPath cannot be blank." }
        project.repo = this
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(Repository::class.java)
    }

    /**
     * Commits that belong to this [Repository].
     *
     * ### Semantics
     * - **Add-only collection:** Backed by `NonRemovingMutableSet` — removal operations
     *   (`remove`, `retainAll`, `clear`, iterator `remove`) are unsupported.
     * - **Repository consistency:** A commit can be added only if `commit.repository == this@Repository`.
     * - **No implicit graph wiring:** Adding a commit here **does not** establish parent/child/branch
     *   relations; those must be handled explicitly elsewhere.
     * - **Set semantics / de-duplication:** Membership is keyed by each commit’s `uniqueKey`
     *   (business key). Re-adding an existing commit is a no-op (`false`). The first instance
     *   for a given key becomes the canonical stored instance.
     *
     * ### Invariants enforced on insert
     * - Precondition: `element.repository == this@Repository`.
     * - Postcondition (on success / no exception):
     *     - `element in commits`
     *
     * ### Bulk adds
     * - `addAll` applies the same checks per element as `add`.
     * - Returns `true` if at least one new commit was added.
     * - **Not transactional:** If inserting one element fails, earlier successful inserts remain.
     *
     * ### Idempotency & recursion safety
     * - Re-adding an already-present commit (same `uniqueKey`) is a no-op and does not trigger
     *   any additional side effects. No mutual/back-linking occurs here, so there is no risk of
     *   infinite recursion.
     *
     * ### Exceptions
     * - Throws [IllegalArgumentException] if a commit from a different repository is added.
     * - Any attempt to remove elements from this collection throws [UnsupportedOperationException].
     *
     * ### Thread-safety
     * - Internally backed by a concurrent map; individual `add`/`contains` operations are safe
     *   for concurrent use. Iteration is **weakly consistent**. Multi-step workflows are **not atomic**;
     *   coordinate externally if you need stronger guarantees.
     */

    val commits: MutableSet<Commit> =
        object : NonRemovingMutableSet<Commit>() {
            /**
             * Adds a Commit to the repository. Relatives (parents, children) have to be added manually!
             * @return true if the set was modified
             */
            override fun add(element: Commit): Boolean {
                // check if commit.repository is same as this
                require(element.repository == this@Repository) {
                    "$element cannot be added to a different repository."
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
     * Branches that belong to this [Repository].
     *
     * # Semantics
     * - **Add-only collection:** Backed by `NonRemovingMutableSet` — removal operations
     *   (`remove`, `retainAll`, `clear`, iterator `remove`) are not supported.
     * - **Repository consistency:** A branch can be added only if `branch.repository == this@Repository`.
     *   This method **does not** mutate `branch.repository`; callers must ensure the branch is created
     *   for this repository.
     * - **No implicit graph wiring:** Adding a branch here does **not** touch its commits or any other
     *   relations; those must be managed elsewhere.
     * - **Set semantics / de-duplication:** Membership is keyed by each branch’s `uniqueKey`
     *   (business key). Re-adding an existing branch is a no-op (`false`). The first instance for a
     *   given key becomes the canonical stored element.
     *
     * # Invariants enforced on insert
     * - Precondition: `element.repository == this@Repository`.
     * - Postcondition (on success / no exception):
     *     - `element in branches`
     * - **No back-links:** This operation does not modify `element.commits` or other associations.
     *
     * # Bulk adds
     * - `addAll` applies the same checks as `add`, element by element.
     * - Returns `true` if at least one new branch was added.
     * - **Not transactional:** If a later element fails (e.g., repo mismatch), earlier successful inserts remain.
     *
     * # Idempotency & recursion safety
     * - Re-adding a branch already present (same `uniqueKey`) returns `false` and has no side effects.
     * - No mutual/back-linking is performed here, so there is no risk of recursive `add` loops.
     *
     * # Exceptions
     * - Throws [IllegalArgumentException] if a branch from a different repository is added.
     * - Any attempt to remove elements throws [UnsupportedOperationException].
     *
     * # Thread-safety
     * - Internally backed by a concurrent map; individual `add`/`contains` calls are safe for concurrent use.
     *   Iteration is **weakly consistent**. Multi-step workflows are **not atomic**; coordinate externally if needed.
     */
    val branches: MutableSet<Branch> =
        object : NonRemovingMutableSet<Branch>() {
            override fun add(element: Branch): Boolean {
                // check if branch has no repository set
                require(element.repository == this@Repository) {
                    "$element cannot be added to a different repository."
                }

                // Add to this repository
                val added = super.add(element)
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

    /**
     * Users that belong to this [Repository].
     *
     * # Semantics
     * - **Add-only collection:** Backed by `NonRemovingMutableSet` — removal operations
     *   (`remove`, `retainAll`, `clear`, iterator `remove`) are not supported.
     * - **Repository consistency:** A user can be added only if `user.repository == this@Repository`.
     *   This method **does not** mutate `user.repository`; callers must ensure the user
     *   is created for this repository.
     * - **No implicit graph wiring:** Adding a user here does **not** touch the user’s
     *   authored/committed commits or any other relations.
     * - **Set semantics / de-duplication:** Membership is keyed by each user’s `uniqueKey`
     *   (business key). Re-adding an existing user is a no-op (`false`). The first instance
     *   for a given key becomes the canonical stored element.
     *
     * # Invariants enforced on insert
     * - Precondition: `element.repository == this@Repository`.
     * - Postcondition (on success / no exception):
     *     - `element in user`.
     * - **No back-links:** This operation does not modify other associations on the user.
     *
     * # Bulk adds
     * - `addAll` applies the same checks as `add`, element by element.
     * - Returns `true` if at least one new user was added.
     * - **Not transactional:** If a later element fails (e.g., repo mismatch), earlier successful inserts remain.
     *
     * # Idempotency & recursion safety
     * - Re-adding a user already present (same `uniqueKey`) returns `false` and has no side effects.
     * - No mutual/back-linking is performed here, so there is no risk of recursive `add` loops.
     *
     * # Exceptions
     * - Throws [IllegalArgumentException] if a user from a different repository is added.
     * - Any attempt to remove elements throws [UnsupportedOperationException].
     *
     * # Thread-safety
     * - Internally backed by a concurrent map; individual `add`/`contains` calls are safe for concurrent use.
     *   Iteration is **weakly consistent**. Multi-step workflows are **not atomic**; coordinate externally if needed.
     */
    val user: MutableSet<User> =
        object : NonRemovingMutableSet<User>() {
            override fun add(element: User): Boolean {
                // check if user has no repository set
                require(element.repository == this@Repository) {
                    "$element cannot be added to a different repository."
                }

                val added = super.add(element)
                return added
            }

            override fun addAll(elements: Collection<User>): Boolean {
                // for bulk-adds make sure each one gets the same treatment
                var anyAdded = false
                for (e in elements) {
                    if (add(e)) anyAdded = true
                }
                return anyAdded
            }
        }

    /**
     * Remotes that belong to this [Repository].
     *
     * # Semantics
     * - **Add-only collection:** Backed by `NonRemovingMutableSet` — removal operations
     *   (`remove`, `retainAll`, `clear`, iterator `remove`) are not supported.
     * - **Repository consistency:** A remote can be added only if `remote.repository == this@Repository`.
     *   This method **does not** mutate `remote.repository`; callers must ensure the remote is created
     *   for this repository.
     * - **No implicit graph wiring:** Adding a remote here does **not** touch any other relations.
     * - **Set semantics / de-duplication:** Membership is keyed by each remote's `uniqueKey`
     *   (business key). Re-adding an existing remote is a no-op (`false`). The first instance for a
     *   given key becomes the canonical stored element.
     *
     * # Invariants enforced on insert
     * - Precondition: `element.repository == this@Repository`.
     * - Postcondition (on success / no exception):
     *     - `element in remotes`
     * - **No back-links:** This operation does not modify other associations on the remote.
     *
     * # Bulk adds
     * - `addAll` applies the same checks as `add`, element by element.
     * - Returns `true` if at least one new remote was added.
     * - **Not transactional:** If a later element fails (e.g., repo mismatch), earlier successful inserts remain.
     *
     * # Idempotency & recursion safety
     * - Re-adding a remote already present (same `uniqueKey`) returns `false` and has no side effects.
     * - No mutual/back-linking is performed here, so there is no risk of recursive `add` loops.
     *
     * # Exceptions
     * - Throws [IllegalArgumentException] if a remote from a different repository is added.
     * - Any attempt to remove elements throws [UnsupportedOperationException].
     *
     * # Thread-safety
     * - Internally backed by a concurrent map; individual `add`/`contains` calls are safe for concurrent use.
     *   Iteration is **weakly consistent**. Multi-step workflows are **not atomic**; coordinate externally if needed.
     */
    val remotes: MutableSet<Remote> =
        object : NonRemovingMutableSet<Remote>() {
            override fun add(element: Remote): Boolean {
                // check if remote has no repository set
                require(element.repository == this@Repository) {
                    "$element cannot be added to a different repository."
                }

                val added = super.add(element)
                return added
            }

            override fun addAll(elements: Collection<Remote>): Boolean {
                // for bulk-adds make sure each one gets the same treatment
                var anyAdded = false
                for (e in elements) {
                    if (add(e)) anyAdded = true
                }
                return anyAdded
            }
        }

    override fun toString(): String = "Repository(id=$id, iid=$iid, localPath='$localPath', project=$project)"

    override val uniqueKey: Key
        get() = Key(project.iid, localPath.trim())

    // Entities compare by immutable identity only
    override fun equals(other: Any?) = super.equals(other)
    override fun hashCode(): Int = super.hashCode()
}
