package com.inso_world.binocular.ffi.extensions

import com.inso_world.binocular.ffi.internal.GixBranch
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository

/**
 * Map an FFI branch into a domain [Branch], **preserving identity**.
 *
 * ### Semantics
 * - **Name normalization:** Strips Git ref prefixes (e.g., `refs/heads/`, `refs/remotes/`, `refs/tags/`)
 *   to produce readable branch names. Examples:
 *   - `refs/heads/main` → `main`
 *   - `refs/remotes/origin/feature` → `origin/feature`
 *   - `refs/tags/v1.0.0` → `v1.0.0`
 *   - `plain-name` → `plain-name` (unchanged)
 * - **Identity-preserving:** Returns the canonical [Branch] for `(repository, name)`.
 *   If none exists, a new one is created; the domain model registers it into
 *   `repository.branches` during `init` (add-only, de-duplicated).
 * - **Head update:** If the branch already exists, its [Branch.head] is updated when
 *   different (setter validates repository consistency).
 *
 * ### Exceptions
 * - [Branch.head] will throw if `head.repository != repository` (domain invariant).
 *
 * @param repository Owning repository in which the branch must reside.
 * @param head The commit to set as the branch head.
 * @return The canonical [Branch] instance for the given repository and name.
 */
internal fun GixBranch.toDomain(repository: Repository, head: Commit): Branch {
    // Try to reuse existing branch identity in this repository
    val existing = repository.branches.firstOrNull { it.uniqueKey.name == this.name }
    if (existing != null) {
        if (existing.head != head) existing.head = head // repository consistency enforced by setter
        return existing
    }

    // Otherwise create a new branch; Branch.init will register it into repository.branches
    return Branch(
        name = this.name,
        fullName = this.fullName.toString(),
        category = this.category.toDomain(),
        repository = repository,
        head = head
    )
}
