package com.inso_world.binocular.model.narrative

/**
 * Value object pointing at the repository entity (commit, issue or build) a narrative event is about.
 *
 * ## Purpose
 * Correlation in a narrative happens between entities, not between single events: a commit is linked
 * to an issue, a build to a commit. One entity can produce several events (an issue is opened and later
 * closed), so events reference each other through the entity they belong to.
 *
 * ## Semantics
 * - [ref] is the natural identifier of the entity within its [source]: the SHA of a commit,
 *   the platform number of an issue, the id of a build.
 * - [ref] alone is not unique across sources; [key] is.
 *
 * ## Value Semantics
 * - This is a **data class** with value-based equality over [source] and [ref].
 * - Immutable after construction.
 *
 * ## Validation
 * - [ref] must not be blank.
 *
 * ## Usage
 * ```kotlin
 * val issue = NarrativeEntityRef(source = NarrativeSource.ITS, ref = "431")
 * issue.key // "ITS:431"
 * ```
 *
 * @property source The data source the entity lives in.
 * @property ref The natural identifier of the entity within [source]; must not be blank.
 * @throws IllegalArgumentException if [ref] is blank.
 * @see NarrativeEvent
 */
data class NarrativeEntityRef(
    val source: NarrativeSource,
    val ref: String,
) {
    init {
        require(ref.isNotBlank()) { "ref must not be blank" }
    }

    /**
     * Identifier of the entity that is unique across all sources of a narrative.
     *
     * @return The key in the form `<source>:<ref>`, e.g. `ITS:431`.
     */
    val key: String
        get() = "$source:$ref"
}
