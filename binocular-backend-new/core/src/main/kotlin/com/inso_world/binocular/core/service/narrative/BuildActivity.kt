package com.inso_world.binocular.core.service.narrative

import java.time.LocalDateTime

/**
 * Raw facts about one CI build, as needed to compose a software history narrative.
 *
 * ## Purpose
 * Read model returned by [NarrativeInfrastructurePort]. It carries the result of a build together with
 * the stored links to the commits it ran for.
 *
 * ## Semantics
 * - [status] is the status exactly as reported by the CI platform (e.g. `success`, `failed`,
 *   `cancelled`). Mapping it to a narrative event type is the job of the narrative service.
 * - [timestamp] is the moment the build was created. It is used instead of the finish time because it is
 *   always present and places a build right after the commit that triggered it.
 * - [commitShas] reflects the stored commit-to-build links; a build without links has an empty list.
 *
 * ## Usage
 * ```kotlin
 * BuildActivity(
 *     id = "982",
 *     status = "failed",
 *     timestamp = LocalDateTime.of(2025, 3, 14, 10, 31),
 *     title = "#431: Add resolver",
 *     actor = "janedoe",
 *     commitShas = listOf("a1b2c3d4"),
 *     webUrl = "https://example.com/actions/runs/982",
 * )
 * ```
 *
 * @property id The identifier of the build on its CI platform.
 * @property status The status as reported by the CI platform, or `null` if unknown.
 * @property timestamp When the build was created (UTC).
 * @property title The display title of the build run, or `null` if the platform provides none.
 * @property actor Display name of who triggered the build, or `null` if unknown.
 * @property commitShas Full SHAs of the commits the build is linked to.
 * @property webUrl Link to the build on the CI platform, or `null` if unknown.
 * @see NarrativeInfrastructurePort.findBuildActivities
 */
data class BuildActivity(
    val id: String,
    val status: String?,
    val timestamp: LocalDateTime,
    val title: String?,
    val actor: String?,
    val commitShas: List<String>,
    val webUrl: String?,
)
