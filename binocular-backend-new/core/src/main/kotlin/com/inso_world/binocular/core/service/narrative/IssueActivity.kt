package com.inso_world.binocular.core.service.narrative

import java.time.LocalDateTime

/**
 * Raw facts about one issue, as needed to compose a software history narrative.
 *
 * ## Purpose
 * Read model returned by [NarrativeInfrastructurePort]. One issue can lead to two narrative events,
 * one for being opened and one for being closed; both are derived from this single record.
 *
 * ## Semantics
 * - [closedAt] is `null` for as long as the issue is open.
 * - The mined data does not record who closed an issue, so the record only names its [author].
 *
 * ## Usage
 * ```kotlin
 * IssueActivity(
 *     number = 431,
 *     title = "GraphQL solver",
 *     createdAt = LocalDateTime.of(2025, 3, 3, 9, 0),
 *     closedAt = LocalDateTime.of(2025, 3, 20, 16, 45),
 *     author = "Jane Doe",
 *     webUrl = "https://example.com/issues/431",
 * )
 * ```
 *
 * @property number The number of the issue on its platform, e.g. `431` for `#431`.
 * @property title The title of the issue.
 * @property createdAt When the issue was opened (UTC).
 * @property closedAt When the issue was closed (UTC), or `null` if it is still open.
 * @property author Display name of who opened the issue, or `null` if unknown.
 * @property webUrl Link to the issue on the hosting platform, or `null` if unknown.
 * @see NarrativeInfrastructurePort.findIssueActivities
 */
data class IssueActivity(
    val number: Int,
    val title: String,
    val createdAt: LocalDateTime,
    val closedAt: LocalDateTime?,
    val author: String?,
    val webUrl: String?,
)
