package com.inso_world.binocular.web.graphql.mapper.impl

import com.inso_world.binocular.model.narrative.NarrativeEvent
import com.inso_world.binocular.model.narrative.NarrativeEventType
import com.inso_world.binocular.model.narrative.NarrativeSegment
import com.inso_world.binocular.web.graphql.model.NarrativeEventDto
import com.inso_world.binocular.web.graphql.model.NarrativeSegmentDto
import org.springframework.stereotype.Component

/**
 * Maps the narrative domain model to its GraphQL wire types.
 *
 * ## Semantics
 * - The mapping is a pure projection: nothing is recomputed that the domain model does not already
 *   define. The key figures of a segment are read from [NarrativeSegment.count], its actors from
 *   [NarrativeSegment.actors].
 * - Entity references are flattened to their key (`SOURCE:ref`).
 *
 * ## Usage
 * Called through the aggregator `GraphQlMapper.toDto(segment)`; do not call it from controllers directly.
 *
 * ```kotlin
 * val dtos = narrativeService.compose(NarrativeGranularity.MONTH).map { mapper.toDto(it) }
 * ```
 */
@Component
class GraphQlNarrativeMapper {
    /**
     * Maps a segment together with all of its events.
     *
     * @param segment The segment to map.
     * @return The wire type with the segment's period, summary, key figures and events in unchanged order.
     */
    fun toDto(segment: NarrativeSegment): NarrativeSegmentDto =
        NarrativeSegmentDto(
            granularity = segment.granularity,
            start = segment.start,
            end = segment.end,
            summary = segment.summary,
            topModule = segment.topModule,
            actors = segment.actors,
            commitCount = segment.count(NarrativeEventType.COMMIT),
            mergeCount = segment.count(NarrativeEventType.MERGE),
            issuesOpened = segment.count(NarrativeEventType.ISSUE_OPENED),
            issuesClosed = segment.count(NarrativeEventType.ISSUE_CLOSED),
            buildsSucceeded = segment.count(NarrativeEventType.BUILD_SUCCEEDED),
            buildsFailed = segment.count(NarrativeEventType.BUILD_FAILED),
            events = segment.events.map { toDto(it) },
        )

    /**
     * Maps a single event.
     *
     * @param event The event to map.
     * @return The wire type with the event's entity and related entities flattened to their keys.
     */
    private fun toDto(event: NarrativeEvent): NarrativeEventDto =
        NarrativeEventDto(
            id = event.id,
            type = event.type,
            source = event.type.source,
            entityKey = event.entity.key,
            timestamp = event.timestamp,
            title = event.title,
            actor = event.actor,
            modules = event.modules,
            relatedEntityKeys = event.relatedEntities.map { it.key },
            webUrl = event.webUrl,
        )
}
