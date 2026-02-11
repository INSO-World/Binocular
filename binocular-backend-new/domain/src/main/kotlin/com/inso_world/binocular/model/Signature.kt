package com.inso_world.binocular.model

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import java.time.LocalDateTime

/**
 * Value object representing a Git signature for commit authorship or committing.
 *
 * ## Purpose
 * A Signature captures the identity ([developer]) and timestamp ([timestamp]) of a Git
 * action (authoring or committing). This is analogous to Git's internal signature format
 * which stores name, email, and timestamp together.
 *
 * ## Git Semantics
 * In Git, every commit has two signatures:
 * - **Author signature**: Who wrote the code (author name, email, and timestamp).
 * - **Committer signature**: Who committed the code (committer name, email, and timestamp).
 * These can differ when patches are applied, commits are cherry-picked, or rebased.
 *
 * ## Value Semantics
 * - This is a **data class** with value-based equality.
 * - Two signatures are equal if they have the same [developer] and [timestamp].
 * - Immutable after construction.
 *
 * ## Validation
 * - [timestamp] must be past or present (no future timestamps allowed).
 * - [developer] must not be null.
 *
 * ## Usage
 * ```kotlin
 * val author = Developer(name = "John", email = "john@example.com", repository = repo)
 * val authorSignature = Signature(developer = author, timestamp = LocalDateTime.now())
 *
 * val commit = Commit(
 *     sha = "abc123...",
 *     authorSignature = authorSignature,
 *     repository = repo
 * )
 * ```
 *
 * @property developer The [Developer] who performed the action.
 * @property timestamp When the action occurred; must be past or present.
 * @see Developer
 * @see Commit
 */
data class Signature(
    @field:NotNull
    val developer: Developer,
    @field:PastOrPresent
    @field:NotNull
    val timestamp: LocalDateTime
) {
    init {
        val now = LocalDateTime.now().plusNanos(1)
        require(timestamp.isBefore(now)) {
            "timestamp ($timestamp) must be past or present ($now)"
        }
    }

    /**
     * Git signature string format.
     * Delegates to the developer's [Developer.gitSignature].
     *
     * @return Formatted string like "Name <email@example.com>"
     */
    val gitSignature: String
        get() = developer.gitSignature
}
