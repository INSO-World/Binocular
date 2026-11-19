package com.inso_world.binocular.web.graphql.model

import com.inso_world.binocular.model.narrative.NarrativeEventType
import com.inso_world.binocular.model.narrative.NarrativeSource
import java.time.LocalDateTime

/**
 * Wire type of the GraphQL type `NarrativeEvent`.
 *
 * ## Purpose
 * Carries one event of a narrative to the client. Entities are flattened to their key (`SOURCE:ref`,
 * e.g. `VCS:<sha>`, `ITS:<issue number>`, `CI:<build id>`) so that a client can match an event's
 * [relatedEntityKeys] against the [entityKey] of other events without knowing the domain model.
 *
 * ## Usage
 * Instances are created by `GraphQlNarrativeMapper` only.
 *
 * @property id Unique within a narrative, of the form `TYPE:ref`.
 * @property type What happened.
 * @property source Data source the event comes from; always the source of [type].
 * @property entityKey Key of the commit, issue or build the event is about.
 * @property timestamp When it happened (UTC).
 * @property title Short text for display: first line of the commit message, issue title or build name.
 * @property actor Name of who caused the event; `null` if unknown.
 * @property modules Top-level directories the event touched.
 * @property relatedEntityKeys Keys of the entities correlated with this event.
 * @property webUrl Link to the entity on its platform; `null` if unknown.
 * @see com.inso_world.binocular.model.narrative.NarrativeEvent
 */
data class NarrativeEventDto(
    val id: String,
    val type: NarrativeEventType,
    val source: NarrativeSource,
    val entityKey: String,
    val timestamp: LocalDateTime,
    val title: String,
    val actor: String?,
    val modules: List<String>,
    val relatedEntityKeys: List<String>,
    val webUrl: String?,
)
