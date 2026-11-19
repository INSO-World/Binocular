package com.inso_world.binocular.web.graphql.model

import com.inso_world.binocular.model.narrative.NarrativeGranularity
import java.time.LocalDateTime

/**
 * Wire type of the GraphQL type `NarrativeSegment`.
 *
 * ## Purpose
 * Carries one period of a narrative to the client. The key figures are stored as plain fields so that a
 * client can draw the timeline from summaries and counts alone and only request [events] for the
 * segment it drills into.
 *
 * ## Semantics
 * The period is half-open: [start] is inclusive, [end] exclusive. All counts refer to [events].
 *
 * ## Usage
 * Instances are created by `GraphQlNarrativeMapper` only.
 *
 * @property granularity Time unit of the period.
 * @property start Start of the period, inclusive (UTC).
 * @property end End of the period, exclusive (UTC).
 * @property summary Template-based micro-summary of the events.
 * @property topModule Module touched by most commits and merges; `null` if none touched a file.
 * @property actors Everyone who acted in the period, most active first.
 * @property commitCount Number of commits that are not merges.
 * @property mergeCount Number of merge commits.
 * @property issuesOpened Number of issues opened.
 * @property issuesClosed Number of issues closed.
 * @property buildsSucceeded Number of builds that succeeded.
 * @property buildsFailed Number of builds that failed.
 * @property events The events of the period in chronological order.
 * @see com.inso_world.binocular.model.narrative.NarrativeSegment
 */
data class NarrativeSegmentDto(
    val granularity: NarrativeGranularity,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val summary: String,
    val topModule: String?,
    val actors: List<String>,
    val commitCount: Int,
    val mergeCount: Int,
    val issuesOpened: Int,
    val issuesClosed: Int,
    val buildsSucceeded: Int,
    val buildsFailed: Int,
    val events: List<NarrativeEventDto>,
)
