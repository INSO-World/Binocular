package com.inso_world.binocular.model.narrative

/**
 * Time granularity by which a narrative is cut into [NarrativeSegment]s.
 *
 * @see NarrativeSegment
 */
enum class NarrativeGranularity {
    /** One segment per calendar month. */
    MONTH,

    /** One segment per ISO-8601 week (Monday to Sunday). */
    WEEK,
}
