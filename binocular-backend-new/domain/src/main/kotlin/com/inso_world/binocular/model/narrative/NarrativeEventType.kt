package com.inso_world.binocular.model.narrative

/**
 * Kind of a [NarrativeEvent], i.e. what happened at a single point in a project's history.
 *
 * ## Semantics
 * - Every type belongs to exactly one [source]; the mapping never changes.
 * - [COMMIT] and [MERGE] are mutually exclusive: a commit with two or more parents is a [MERGE],
 *   every other commit is a [COMMIT].
 * - An issue produces up to two events over its lifetime: [ISSUE_OPENED] and, once closed, [ISSUE_CLOSED].
 * - A build produces exactly one event describing its result.
 *
 * @property source The data source this type of event originates from.
 * @see NarrativeEvent
 */
enum class NarrativeEventType(
    val source: NarrativeSource,
) {
    /** A commit with at most one parent. */
    COMMIT(NarrativeSource.VCS),

    /** A commit with two or more parents. */
    MERGE(NarrativeSource.VCS),

    /** An issue was created. */
    ISSUE_OPENED(NarrativeSource.ITS),

    /** An issue was closed. */
    ISSUE_CLOSED(NarrativeSource.ITS),

    /** A CI build finished successfully. */
    BUILD_SUCCEEDED(NarrativeSource.CI),

    /** A CI build finished with a failure. */
    BUILD_FAILED(NarrativeSource.CI),
}
