package com.inso_world.binocular.model.narrative

/**
 * Data source a narrative entity or event originates from.
 *
 * ## Purpose
 * A software history narrative combines three kinds of repository data. Every [NarrativeEventType]
 * belongs to exactly one source, which allows clients to switch whole sources on and off.
 *
 * @see NarrativeEventType
 * @see NarrativeEntityRef
 */
enum class NarrativeSource {
    /** Version control system: commits and merges. */
    VCS,

    /** Issue tracking system: issue lifecycle events. */
    ITS,

    /** Continuous integration: build results. */
    CI,
}
