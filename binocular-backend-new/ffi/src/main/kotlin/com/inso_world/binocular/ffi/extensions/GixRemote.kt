package com.inso_world.binocular.ffi.extensions

import com.inso_world.binocular.ffi.internal.GixRemote
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.vcs.Remote

/**
 * Converts a Rust FFI [GixRemote] to a domain [Remote] model with identity preservation.
 *
 * ### Semantics
 * - **Find-or-create pattern:** Searches for an existing [Remote] in [repository].[remotes][Repository.remotes]
 *   by matching [name]. If found, returns the existing instance; otherwise creates a new one.
 * - **URL synchronization:** If an existing remote is found and its URL differs from `this.url`,
 *   the existing remote's URL is updated to match the FFI remote's URL.
 * - **Identity preservation:** Ensures that for a given repository + name combination, there is
 *   always exactly one canonical [Remote] instance. Prevents orphaned instances that aren't
 *   in the repository's remotes collection.
 *
 * ### Invariants & requirements
 * - **Precondition:** [repository] must be a valid, non-null repository.
 * - **Postcondition (existing remote found):**
 *   - Returns the existing remote instance (same object identity via `===`).
 *   - `result.url == this.url` (URL updated if it was different).
 *   - `result in repository.remotes` (already present).
 * - **Postcondition (new remote created):**
 *   - Returns a newly constructed [Remote] instance.
 *   - `result in repository.remotes` (auto-added during `Remote` construction).
 *   - `repository.remotes.size` increases by 1.
 *
 * ### Trade-offs & guidance
 * - **Linear search:** Uses `find` to search through `repository.remotes` (O(n) worst case).
 *   For repositories with many remotes (typically 1-5), this is acceptable. If performance
 *   becomes an issue, consider indexing remotes by name.
 * - **Mutation of existing:** Updates the URL of existing remotes in-place. This is intentional
 *   to reflect changes in the Git repository's remote configuration.
 * - **Thread-safety:** The `find` + conditional create/update is **not atomic**. If concurrent
 *   threads call this method for the same remote name, duplicate instances might be created
 *   temporarily. The `NonRemovingMutableSet` will only keep one based on `uniqueKey`, but
 *   callers should coordinate externally if this is a concern.
 *
 * ### Example
 * ```kotlin
 * val repository = Repository(localPath = "/path/to/repo", project = myProject)
 *
 * // First call: creates new remote
 * val ffiRemote = GixRemote(name = "origin", url = "https://github.com/user/repo.git")
 * val remote1 = ffiRemote.toModel(repository)
 * check(remote1 in repository.remotes)
 * check(repository.remotes.size == 1)
 *
 * // Second call with same name: returns existing remote
 * val ffiRemote2 = GixRemote(name = "origin", url = "https://github.com/user/repo.git")
 * val remote2 = ffiRemote2.toModel(repository)
 * check(remote1 === remote2)  // Same instance
 * check(repository.remotes.size == 1)  // No duplicate
 *
 * // Call with updated URL: updates existing remote
 * val ffiRemote3 = GixRemote(name = "origin", url = "git@github.com:user/repo.git")
 * val remote3 = ffiRemote3.toModel(repository)
 * check(remote1 === remote3)  // Still same instance
 * check(remote3.url == "git@github.com:user/repo.git")  // URL updated
 * ```
 *
 * @param repository The domain repository to which this FFI remote belongs.
 * @return The canonical [Remote] instance for this name, either existing or newly created.
 */
internal fun GixRemote.toModel(repository: Repository): Remote {
    // Find existing remote by name (business key component)
    val existing = repository.remotes.find { it.name == this.name }

    return if (existing != null) {
        // Identity preservation: return existing instance
        // Update URL if it changed (synchronize with Git remote config)
        if (existing.url != this.url) {
            existing.url = this.url
        }
        existing
    } else {
        // Create new remote (auto-adds to repository.remotes in Remote's init block)
        Remote(
            name = this.name,
            url = this.url,
            repository = repository
        )
    }
}

internal fun Remote.toFfi(): GixRemote = GixRemote(
    name = this.name,
    url = this.url,
)
