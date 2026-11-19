package com.inso_world.binocular.web.graphql.mapper

import com.inso_world.binocular.core.unit.base.BaseUnitTest
import com.inso_world.binocular.model.narrative.NarrativeEntityRef
import com.inso_world.binocular.model.narrative.NarrativeEvent
import com.inso_world.binocular.model.narrative.NarrativeEventType
import com.inso_world.binocular.model.narrative.NarrativeGranularity
import com.inso_world.binocular.model.narrative.NarrativeSegment
import com.inso_world.binocular.model.narrative.NarrativeSource
import com.inso_world.binocular.web.graphql.mapper.impl.GraphQlNarrativeMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.LocalDateTime

/**
 * BDD tests for [GraphQlNarrativeMapper].
 * The mapper is a pure projection, so the tests run without a Spring context.
 */
internal class GraphQlNarrativeMapperTest : BaseUnitTest() {
    private val mapper = GraphQlNarrativeMapper()
    private val start = LocalDateTime.of(2025, 3, 1, 0, 0)

    /** Creates an event of the given type whose entity is named after the type and its position. */
    private fun event(
        type: NarrativeEventType,
        minute: Long,
        actor: String? = null,
        relatedEntities: List<NarrativeEntityRef> = emptyList(),
    ) = NarrativeEvent(
        type = type,
        entity = NarrativeEntityRef(type.source, "$type-$minute"),
        timestamp = start.plusMinutes(minute),
        title = "title $minute",
        actor = actor,
        modules = listOf("web"),
        relatedEntities = relatedEntities,
        webUrl = "https://example.org/$minute",
    )

    /** A segment with one event per type except two commits must yield matching key figures and actors. */
    @Test
    fun `given a segment, when mapping, then period, summary, key figures and actors are projected`() {
        // Given
        val types = listOf(NarrativeEventType.COMMIT) + NarrativeEventType.entries
        val events = types.mapIndexed { index, type -> event(type, index.toLong(), actor = if (index < 2) "Ada" else "Bob") }
        val segment =
            NarrativeSegment(NarrativeGranularity.MONTH, start, start.plusMonths(1), events, summary = "summary", topModule = "web")

        // When
        val dto = mapper.toDto(segment)

        // Then
        assertAll(
            { assertThat(dto.granularity).isEqualTo(NarrativeGranularity.MONTH) },
            { assertThat(dto.start).isEqualTo(start) },
            { assertThat(dto.end).isEqualTo(start.plusMonths(1)) },
            { assertThat(dto.summary).isEqualTo("summary") },
            { assertThat(dto.topModule).isEqualTo("web") },
            { assertThat(dto.actors).containsExactly("Bob", "Ada") },
            { assertThat(dto.commitCount).isEqualTo(2) },
            { assertThat(dto.mergeCount).isEqualTo(1) },
            { assertThat(dto.issuesOpened).isEqualTo(1) },
            { assertThat(dto.issuesClosed).isEqualTo(1) },
            { assertThat(dto.buildsSucceeded).isEqualTo(1) },
            { assertThat(dto.buildsFailed).isEqualTo(1) },
            { assertThat(dto.events.map { it.id }).containsExactlyElementsOf(events.map { it.id }) },
        )
    }

    /** The entity and the related entities of an event are flattened to keys of the form SOURCE:ref. */
    @Test
    fun `given an event with related entities, when mapping, then entities are flattened to their keys`() {
        // Given
        val related = listOf(NarrativeEntityRef(NarrativeSource.ITS, "101"), NarrativeEntityRef(NarrativeSource.CI, "7"))
        val commit = event(NarrativeEventType.COMMIT, minute = 5, actor = "Ada", relatedEntities = related)
        val segment = NarrativeSegment(NarrativeGranularity.WEEK, start, start.plusWeeks(1), listOf(commit))

        // When
        val dto = mapper.toDto(segment).events.single()

        // Then
        assertAll(
            { assertThat(dto.id).isEqualTo("COMMIT:COMMIT-5") },
            { assertThat(dto.type).isEqualTo(NarrativeEventType.COMMIT) },
            { assertThat(dto.source).isEqualTo(NarrativeSource.VCS) },
            { assertThat(dto.entityKey).isEqualTo("VCS:COMMIT-5") },
            { assertThat(dto.timestamp).isEqualTo(start.plusMinutes(5)) },
            { assertThat(dto.title).isEqualTo("title 5") },
            { assertThat(dto.actor).isEqualTo("Ada") },
            { assertThat(dto.modules).containsExactly("web") },
            { assertThat(dto.relatedEntityKeys).containsExactly("ITS:101", "CI:7") },
            { assertThat(dto.webUrl).isEqualTo("https://example.org/5") },
        )
    }
}
