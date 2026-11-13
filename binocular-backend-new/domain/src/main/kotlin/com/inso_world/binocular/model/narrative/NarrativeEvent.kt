package com.inso_world.binocular.model.narrative

import java.time.LocalDateTime

/**
 * Value object for one human-readable event in the history of a software project.
 *
 * ## Purpose
 * A narrative event is the atomic building block of a software history narrative: a commit, a merge,
 * an issue being opened or closed, or a build result. Events are derived from the mined repository data
 * on request and are never persisted.
 *
 * ## Semantics
 * - [entity] names the commit, issue or build the event is about; its source always equals the source
 *   of [type].
 * - [relatedEntities] lists the entities this event is correlated with, e.g. the issues a commit refers to
 *   or the commit a build ran for. Related entities may have their events in other segments.
 * - [id] is unique within a narrative because an entity produces at most one event per [type].
 *
 * ## Value Semantics
 * - This is a **data class** with value-based equality over all constructor properties.
 * - Immutable after construction.
 *
 * ## Validation
 * - The source of [entity] must match the source of [type].
 * - [relatedEntities] must not contain [entity] itself.
 *
 * ## Trade-offs
 * Events reference entities instead of other events. A client has to look up the events of a related
 * entity itself, but a relation stays valid even if the related events lie in another segment.
 *
 * ## Usage
 * ```kotlin
 * val commit = NarrativeEvent(
 *     type = NarrativeEventType.COMMIT,
 *     entity = NarrativeEntityRef(NarrativeSource.VCS, "a1b2c3d"),
 *     timestamp = LocalDateTime.of(2025, 3, 14, 10, 30),
 *     title = "#431: Add resolver",
 *     actor = "Jane Doe",
 *     modules = listOf("binocular-backend-new/web"),
 *     relatedEntities = listOf(NarrativeEntityRef(NarrativeSource.ITS, "431")),
 * )
 * commit.id // "COMMIT:a1b2c3d"
 * ```
 *
 * @property type What happened.
 * @property entity The commit, issue or build the event is about.
 * @property timestamp When the event happened (UTC, like all timestamps of the mined data).
 * @property title Short human-readable description, e.g. the first line of a commit message or an issue title.
 * @property actor Display name of who caused the event, or `null` if unknown.
 * @property modules Paths of the modules affected by the event; empty if not applicable or unknown.
 * @property relatedEntities Entities this event is correlated with; must not contain [entity].
 * @property webUrl Link to the event on the hosting platform, or `null` if unknown.
 * @throws IllegalArgumentException if the source of [entity] differs from the source of [type],
 *   or if [relatedEntities] contains [entity].
 * @see NarrativeSegment
 * @see NarrativeEntityRef
 */
data class NarrativeEvent(
    val type: NarrativeEventType,
    val entity: NarrativeEntityRef,
    val timestamp: LocalDateTime,
    val title: String,
    val actor: String? = null,
    val modules: List<String> = emptyList(),
    val relatedEntities: List<NarrativeEntityRef> = emptyList(),
    val webUrl: String? = null,
) {
    init {
        require(entity.source == type.source) {
            "entity source (${entity.source}) must match the source of type $type (${type.source})"
        }
        require(entity !in relatedEntities) {
            "relatedEntities must not contain the event's own entity (${entity.key})"
        }
    }

    /**
     * Identifier of the event that is unique within a narrative.
     *
     * @return The id in the form `<type>:<entity ref>`, e.g. `ISSUE_CLOSED:431`.
     */
    val id: String
        get() = "$type:${entity.ref}"
}
