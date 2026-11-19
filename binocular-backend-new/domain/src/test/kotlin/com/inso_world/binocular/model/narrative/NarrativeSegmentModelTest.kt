package com.inso_world.binocular.model.narrative

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

/**
 * BDD tests for the NarrativeSegment value object.
 * A NarrativeSegment is one period (month or week) of a narrative with its chronologically ordered events.
 */
class NarrativeSegmentModelTest {
    private val start = LocalDateTime.of(2025, 3, 1, 0, 0)
    private val end = LocalDateTime.of(2025, 4, 1, 0, 0)

    private fun event(
        type: NarrativeEventType,
        ref: String,
        timestamp: LocalDateTime,
        actor: String? = null,
    ) = NarrativeEvent(
        type = type,
        entity = NarrativeEntityRef(source = type.source, ref = ref),
        timestamp = timestamp,
        title = "$type $ref",
        actor = actor,
    )

    private fun segment(vararg events: NarrativeEvent) =
        NarrativeSegment(
            granularity = NarrativeGranularity.MONTH,
            start = start,
            end = end,
            events = events.toList(),
        )

    /** A period without activity is a valid segment; summary, top module and actors start out empty. */
    @Test
    fun `given no events, when creating segment, then it is valid and optional properties are empty`() {
        // When
        val segment = segment()

        // Then
        assertAll(
            { assertThat(segment.events).isEmpty() },
            { assertThat(segment.summary).isEmpty() },
            { assertThat(segment.topModule).isNull() },
            { assertThat(segment.actors).isEmpty() },
        )
    }

    /** Summary and top module are stored as given; they are not derived from the events. */
    @Test
    fun `given summary and top module, when creating segment, then they are kept`() {
        // When
        val segment = segment().copy(summary = "1 commit by 1 contributor.", topModule = "web")

        // Then
        assertAll(
            { assertThat(segment.summary).isEqualTo("1 commit by 1 contributor.") },
            { assertThat(segment.topModule).isEqualTo("web") },
        )
    }

    @Nested
    inner class Period {
        /** An empty period (start equal to end) and a reversed period (start after end) are both rejected. */
        @Test
        fun `given start not before end, when creating segment, then it throws IllegalArgumentException`() {
            // When & Then
            assertAll(
                {
                    assertThrows<IllegalArgumentException> {
                        NarrativeSegment(NarrativeGranularity.MONTH, start = start, end = start, events = emptyList())
                    }
                },
                {
                    assertThrows<IllegalArgumentException> {
                        NarrativeSegment(NarrativeGranularity.MONTH, start = end, end = start, events = emptyList())
                    }
                },
            )
        }

        /** The start of the period is inclusive: an event exactly at start belongs to the segment. */
        @Test
        fun `given event exactly at start, when creating segment, then it succeeds`() {
            // When & Then
            assertDoesNotThrow { segment(event(NarrativeEventType.COMMIT, "a1", start)) }
        }

        /** An event one nanosecond before start belongs to the previous segment; the error names it. */
        @Test
        fun `given event before start, when creating segment, then it throws IllegalArgumentException`() {
            // When
            val exception =
                assertThrows<IllegalArgumentException> {
                    segment(event(NarrativeEventType.COMMIT, "a1", start.minusNanos(1)))
                }

            // Then
            assertThat(exception.message).contains("COMMIT:a1", start.minusNanos(1).toString())
        }

        /** The end of the period is exclusive: an event exactly at end belongs to the next segment. */
        @Test
        fun `given event exactly at end, when creating segment, then it throws IllegalArgumentException`() {
            // When & Then
            assertThrows<IllegalArgumentException> { segment(event(NarrativeEventType.COMMIT, "a1", end)) }
        }
    }

    @Nested
    inner class Events {
        /** Events at the same moment are allowed and keep the order they were given in. */
        @Test
        fun `given events with equal timestamps, when creating segment, then given order is kept`() {
            // Given
            val commit = event(NarrativeEventType.COMMIT, "a1", start.plusDays(2))
            val build = event(NarrativeEventType.BUILD_SUCCEEDED, "982", start.plusDays(2))

            // When
            val segment = segment(commit, build)

            // Then
            assertThat(segment.events).containsExactly(commit, build)
        }

        /** A later event listed before an earlier one breaks the chronological narrative; the error names both. */
        @Test
        fun `given events out of chronological order, when creating segment, then it throws IllegalArgumentException`() {
            // Given
            val earlier = event(NarrativeEventType.COMMIT, "a1", start.plusDays(1))
            val later = event(NarrativeEventType.COMMIT, "b2", start.plusDays(2))

            // When
            val exception = assertThrows<IllegalArgumentException> { segment(later, earlier) }

            // Then
            assertThat(exception.message).contains("COMMIT:b2 is after COMMIT:a1")
        }

        /** The same event listed twice would be counted twice and is rejected; the error names it. */
        @Test
        fun `given two events with the same id, when creating segment, then it throws IllegalArgumentException`() {
            // Given
            val commit = event(NarrativeEventType.COMMIT, "a1", start.plusDays(1))

            // When
            val exception = assertThrows<IllegalArgumentException> { segment(commit, commit.copy(title = "duplicate")) }

            // Then
            assertThat(exception.message).contains("COMMIT:a1 occurs 2 times")
        }

        /** Each event type is counted on its own; a type without events (here MERGE) counts zero. */
        @Test
        fun `given mixed events, when counting by type, then each type has its own count`() {
            // Given
            val segment =
                segment(
                    event(NarrativeEventType.ISSUE_OPENED, "431", start.plusDays(1)),
                    event(NarrativeEventType.COMMIT, "a1", start.plusDays(2)),
                    event(NarrativeEventType.BUILD_FAILED, "981", start.plusDays(2).plusMinutes(5)),
                    event(NarrativeEventType.COMMIT, "b2", start.plusDays(3)),
                    event(NarrativeEventType.ISSUE_CLOSED, "431", start.plusDays(5)),
                )

            // Then
            assertAll(
                { assertThat(segment.count(NarrativeEventType.COMMIT)).isEqualTo(2) },
                { assertThat(segment.count(NarrativeEventType.MERGE)).isEqualTo(0) },
                { assertThat(segment.count(NarrativeEventType.ISSUE_OPENED)).isEqualTo(1) },
                { assertThat(segment.count(NarrativeEventType.ISSUE_CLOSED)).isEqualTo(1) },
                { assertThat(segment.count(NarrativeEventType.BUILD_FAILED)).isEqualTo(1) },
            )
        }

        /**
         * Actors are listed once each, most events first (Carol has two) and alphabetically on a tie
         * (Alice and Bob have one event each); the build without an actor is ignored.
         */
        @Test
        fun `given events of several actors, when reading actors, then they are distinct and most active first`() {
            // Given
            val segment =
                segment(
                    event(NarrativeEventType.COMMIT, "a1", start.plusDays(1), actor = "Carol"),
                    event(NarrativeEventType.ISSUE_OPENED, "431", start.plusDays(2), actor = "Bob"),
                    event(NarrativeEventType.BUILD_FAILED, "981", start.plusDays(2).plusMinutes(5)),
                    event(NarrativeEventType.COMMIT, "b2", start.plusDays(3), actor = "Alice"),
                    event(NarrativeEventType.COMMIT, "c3", start.plusDays(4), actor = "Carol"),
                )

            // Then
            assertThat(segment.actors).containsExactly("Carol", "Alice", "Bob")
        }
    }
}
