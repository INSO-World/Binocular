package com.inso_world.binocular.model.narrative

import java.time.LocalDateTime

/**
 * Value object for one time-bounded chapter of a software history narrative.
 *
 * ## Purpose
 * A narrative segment groups all [NarrativeEvent]s of one period (a month or a week) and carries what is
 * needed to present that period: a textual summary, the most changed module, and the events themselves
 * for drill-down. Segments are derived from the mined repository data on request and are never persisted.
 *
 * ## Semantics
 * - The period is the half-open interval from [start] (inclusive) to [end] (exclusive), so consecutive
 *   segments never overlap.
 * - [events] are in chronological order; events with the same timestamp keep the order they were given in.
 * - A segment without events is valid and describes a period without activity.
 *
 * ## Value Semantics
 * - This is a **data class** with value-based equality over all constructor properties.
 * - Immutable after construction.
 *
 * ## Validation
 * - [start] must be before [end].
 * - Every event must lie within the period.
 * - [events] must be sorted by timestamp in ascending order.
 * - Event ids must be unique within the segment.
 *
 * ## Trade-offs
 * Key figures ([count]) and contributors ([actors]) are derived from [events] instead of being stored.
 * They are recomputed on every access, but they can never contradict the events.
 *
 * ## Usage
 * ```kotlin
 * val march = NarrativeSegment(
 *     granularity = NarrativeGranularity.MONTH,
 *     start = LocalDateTime.of(2025, 3, 1, 0, 0),
 *     end = LocalDateTime.of(2025, 4, 1, 0, 0),
 *     events = listOf(commit, buildFailed),
 *     summary = "1 commit, 1 failed build",
 * )
 * march.count(NarrativeEventType.COMMIT) // 1
 * march.actors // ["Jane Doe"]
 * ```
 *
 * @property granularity The granularity the narrative was segmented by.
 * @property start Beginning of the period, inclusive (UTC).
 * @property end End of the period, exclusive (UTC); must be after [start].
 * @property events The events of the period in chronological order.
 * @property summary Short textual description of the period; empty until a summary has been composed.
 * @property topModule Path of the most changed module of the period, or `null` if none.
 * @throws IllegalArgumentException if [start] is not before [end], an event lies outside the period,
 *   [events] are not in chronological order, or two events share the same id.
 * @see NarrativeEvent
 * @see NarrativeGranularity
 */
data class NarrativeSegment(
    val granularity: NarrativeGranularity,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val events: List<NarrativeEvent>,
    val summary: String = "",
    val topModule: String? = null,
) {
    init {
        require(start.isBefore(end)) { "start ($start) must be before end ($end)" }
        val outside = events.firstOrNull { it.timestamp.isBefore(start) || !it.timestamp.isBefore(end) }
        require(outside == null) {
            "event ${outside?.id} (${outside?.timestamp}) lies outside the period [$start, $end)"
        }
        val unordered = events.zipWithNext().firstOrNull { (earlier, later) -> earlier.timestamp.isAfter(later.timestamp) }
        require(unordered == null) {
            "events must be in chronological order, but ${unordered?.first?.id} is after ${unordered?.second?.id}"
        }
        val duplicate =
            events
                .groupingBy { it.id }
                .eachCount()
                .entries
                .firstOrNull { it.value > 1 }
        require(duplicate == null) {
            "event ids must be unique within a segment, but ${duplicate?.key} occurs ${duplicate?.value} times"
        }
    }

    /**
     * Display names of everyone who caused at least one event in this segment, most active first.
     *
     * Activity is the number of events a person caused. People with the same number of events are
     * ordered alphabetically, so the result is stable. Events without an actor are ignored.
     *
     * @return The distinct actors of the segment; empty if no event has an actor.
     */
    val actors: List<String>
        get() =
            events
                .mapNotNull { it.actor }
                .groupingBy { it }
                .eachCount()
                .entries
                .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                .map { it.key }

    /**
     * Counts the events of the given [type] in this segment.
     *
     * @param type The kind of event to count.
     * @return The number of events of [type]; `0` if there are none.
     */
    fun count(type: NarrativeEventType): Int = events.count { it.type == type }
}
