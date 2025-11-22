package com.inso_world.binocular.ffi.extensions

import com.inso_world.binocular.ffi.internal.GixCommit
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository

/**
 * Validates that this string is a valid 40-character hexadecimal SHA-1 hash.
 *
 * @throws IllegalArgumentException if the string is not exactly 40 hex characters
 */
private fun String.validateSha(): String {
    require(this.length == 40) {
        "Invalid SHA '$this': must be exactly 40 characters, got ${this.length}"
    }
    require(this.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) {
        "Invalid SHA '$this': must contain only hexadecimal characters [0-9a-fA-F]"
    }
    return this
}

/**
 * Map a single FFI commit into a domain [Commit].
 *
 * ### Semantics
 * - Reuses an existing [Commit] from [Repository.commits] by `sha`, otherwise creates one.
 * - Sets scalars (`sha`, `authorDateTime`, `commitDateTime`, `message`).
 * - Sets `author` / `committer` if provided (respecting set-once + repository consistency).
 * - Does **not** wire parents; the batch mapper handles graph wiring.
 *
 * ### Timestamp requirements
 * - `commitDateTime` is **required**: Must have `committer.time` present.
 * - `authorDateTime` is **optional**: Set directly from `author.time` if present, else `null`.
 *
 * ### Performance note
 * This single-item mapper uses O(n) lookup when no index is provided. For batch operations,
 * prefer [Collection<GixCommit>.toDomain] which uses O(1) indexed lookup.
 *
 * @param repository The owning repository
 * @param shaIndex Optional pre-built SHA index for O(1) lookup; used internally by batch mapper
 * @throws IllegalArgumentException if SHA format is invalid or `committer.time` is null
 */
internal fun GixCommit.toDomain(
    repository: Repository,
    shaIndex: Map<String, Commit>? = null
): Commit {
    // Validate SHA format early at boundary
    this.oid.validateSha()

    val author = this.author?.toDomain(repository)
    val committer = this.committer?.toDomain(repository)

    val authorDt = this.author?.time?.toLocalDateTime()
    val commitDt = requireNotNull(this.committer?.time) {
        "Commit ${this.oid} requires committer timestamp"
    }.toLocalDateTime()

    // Use index if provided (O(1)), otherwise fall back to linear search (O(n))
    val existing = shaIndex?.get(this.oid)
        ?: repository.commits.firstOrNull { it.sha == this.oid }

    val commit =
        existing ?: Commit(
            sha = this.oid,
            authorDateTime = authorDt,
            commitDateTime = commitDt,
            message = this.message,
            repository = repository,
            committer = requireNotNull(this.committer).toDomain(repository)
        )

    // set-once; idempotent if already equal
    author?.let { if (commit.author == null) commit.author = it }

    return commit
}

/**
 * Map a batch of FFI commits into domain [Commit]s while **preserving identity**.
 *
 * ### Strategy
 * 1) **Pass 1 (materialize):** For every element call the single-item mapper
 *    [GixCommit.toDomain]. This reuses/creates canonical [Commit] instances and
 *    sets author/committer identically to the single-item logic.
 * 2) **Pass 2 (wire graph):** Wire `parents` edges. All referenced parent commits must already
 *    exist in either the batch (from Pass 1) or the repository.
 *
 * ### Parent commit requirements
 * - All parent SHAs referenced in `parents` must exist in the repository or batch.
 * - Missing parents will cause a [NoSuchElementException] from `getValue()`.
 * - Process commits in topological order (parents before children) or ensure parents pre-exist.
 *
 * ### Guarantees
 * - Identity is preserved via `Repository.commits` as the canonical set.
 * - Returns commits in the same order as the input collection.
 * - Uses O(1) indexed lookup for efficiency.
 *
 * @throws NoSuchElementException if a parent SHA is referenced but not found in batch or repository
 */
internal fun Collection<GixCommit>.toDomain(repository: Repository): List<Commit> {
    // Seed a quick lookup from existing repo state (canonical instances).
    val bySha = repository.commits.associateBy { it.uniqueKey.sha }.toMutableMap()

    // ---- Pass 1: materialize commits using the single-item mapper with index ----
    val mappedInOrder: List<Commit> = this.map { vec ->
        val c = vec.toDomain(repository, bySha) // <— reuse single-item logic with O(1) lookup
        bySha.putIfAbsent(c.sha, c)
        c
    }

    // ---- Pass 2: wire parent edges (and implicit child back-links by domain invariants) ----
    this.forEach { vec ->
        val child = bySha.getValue(vec.oid)
        vec.parents.forEach { parentSha ->
            parentSha.validateSha() // Validate parent SHA early
            val parent = bySha.getValue(parentSha)
            child.parents.add(parent) // domain ensures repository consistency & back-link to children
        }
    }

    return mappedInOrder
}
