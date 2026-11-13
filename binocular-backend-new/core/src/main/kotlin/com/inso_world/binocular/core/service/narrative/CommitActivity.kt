package com.inso_world.binocular.core.service.narrative

import java.time.LocalDateTime

/**
 * Raw facts about one commit, as needed to compose a software history narrative.
 *
 * ## Purpose
 * Read model returned by [NarrativeInfrastructurePort]. It carries only what the narrative needs from
 * a commit and its stored relations, so that a whole history can be loaded with a single bulk query
 * instead of resolving a full commit aggregate per commit.
 *
 * ## Semantics
 * - The record is uninterpreted: whether the commit counts as a merge, which modules it touched and
 *   what its title is are decided by the narrative service, not by the adapter that fills the record.
 * - [issueNumbers] reflects the stored commit-to-issue links; a commit without links has an empty list.
 *
 * ## Usage
 * ```kotlin
 * CommitActivity(
 *     sha = "a1b2c3d4",
 *     timestamp = LocalDateTime.of(2025, 3, 14, 10, 30),
 *     message = "#431: Add resolver\n\nDetails ...",
 *     author = "Jane Doe",
 *     parentCount = 1,
 *     filePaths = listOf("binocular-backend-new/web/src/main/kotlin/Resolver.kt"),
 *     issueNumbers = listOf(431),
 *     webUrl = "https://example.com/commit/a1b2c3d4",
 * )
 * ```
 *
 * @property sha The full SHA of the commit.
 * @property timestamp When the commit was made (UTC).
 * @property message The full commit message.
 * @property author Display name of the commit author, or `null` if unknown.
 * @property parentCount Number of parent commits; two or more for a merge.
 * @property filePaths Repository-relative paths of the files changed by the commit.
 * @property issueNumbers Platform numbers of the issues the commit is linked to.
 * @property webUrl Link to the commit on the hosting platform, or `null` if unknown.
 * @see NarrativeInfrastructurePort.findCommitActivities
 */
data class CommitActivity(
    val sha: String,
    val timestamp: LocalDateTime,
    val message: String,
    val author: String?,
    val parentCount: Int,
    val filePaths: List<String>,
    val issueNumbers: List<Int>,
    val webUrl: String?,
)
