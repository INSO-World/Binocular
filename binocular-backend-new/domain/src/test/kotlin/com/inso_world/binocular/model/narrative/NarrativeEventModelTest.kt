package com.inso_world.binocular.model.narrative

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDateTime

/**
 * BDD tests for the NarrativeEvent value object and its NarrativeEventType.
 * A NarrativeEvent is one commit, merge, issue lifecycle step or build result in a project's history.
 */
class NarrativeEventModelTest {
    private val timestamp = LocalDateTime.of(2025, 3, 14, 10, 30)
    private val commitEntity = NarrativeEntityRef(source = NarrativeSource.VCS, ref = "a1b2c3d")
    private val issueEntity = NarrativeEntityRef(source = NarrativeSource.ITS, ref = "431")

    /** Every event type is assigned to the data source it is mined from. */
    @ParameterizedTest
    @CsvSource(
        "COMMIT, VCS",
        "MERGE, VCS",
        "ISSUE_OPENED, ITS",
        "ISSUE_CLOSED, ITS",
        "BUILD_SUCCEEDED, CI",
        "BUILD_FAILED, CI",
    )
    fun `given event type, when reading source, then it is the source the event is mined from`(
        type: NarrativeEventType,
        expectedSource: NarrativeSource,
    ) {
        // Then
        assertThat(type.source).isEqualTo(expectedSource)
    }

    /** Only type, entity, timestamp and title are required; everything else has an empty default. */
    @Test
    fun `given only required properties, when creating event, then optional properties are empty`() {
        // When
        val event =
            NarrativeEvent(
                type = NarrativeEventType.COMMIT,
                entity = commitEntity,
                timestamp = timestamp,
                title = "Add resolver",
            )

        // Then
        assertAll(
            { assertThat(event.actor).isNull() },
            { assertThat(event.modules).isEmpty() },
            { assertThat(event.relatedEntities).isEmpty() },
            { assertThat(event.webUrl).isNull() },
        )
    }

    /** Everything an event is created with can be read back unchanged. */
    @Test
    fun `given all properties, when creating event, then they are kept`() {
        // When
        val event =
            NarrativeEvent(
                type = NarrativeEventType.COMMIT,
                entity = commitEntity,
                timestamp = timestamp,
                title = "Add resolver",
                actor = "Jane Doe",
                modules = listOf("web"),
                relatedEntities = listOf(issueEntity),
                webUrl = "https://example.org/commit/a1b2c3d",
            )

        // Then
        assertAll(
            { assertThat(event.title).isEqualTo("Add resolver") },
            { assertThat(event.actor).isEqualTo("Jane Doe") },
            { assertThat(event.modules).containsExactly("web") },
            { assertThat(event.relatedEntities).containsExactly(issueEntity) },
            { assertThat(event.webUrl).isEqualTo("https://example.org/commit/a1b2c3d") },
        )
    }

    /** An event type can only describe an entity of its own source, e.g. no commit event about an issue. */
    @ParameterizedTest
    @CsvSource("COMMIT, ITS", "ISSUE_CLOSED, CI", "BUILD_FAILED, VCS")
    fun `given entity of another source, when creating event, then it throws IllegalArgumentException`(
        type: NarrativeEventType,
        entitySource: NarrativeSource,
    ) {
        // When & Then
        assertThrows<IllegalArgumentException> {
            NarrativeEvent(
                type = type,
                entity = NarrativeEntityRef(source = entitySource, ref = "1"),
                timestamp = timestamp,
                title = "title",
            )
        }
    }

    /** An event cannot be correlated with the entity it is itself about. */
    @Test
    fun `given own entity among related entities, when creating event, then it throws IllegalArgumentException`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            NarrativeEvent(
                type = NarrativeEventType.COMMIT,
                entity = commitEntity,
                timestamp = timestamp,
                title = "Add resolver",
                relatedEntities = listOf(issueEntity, commitEntity),
            )
        }
    }

    /** The id joins type and entity ref, so opening and closing issue 431 get different ids. */
    @ParameterizedTest
    @CsvSource("COMMIT, a1b2c3d, COMMIT:a1b2c3d", "ISSUE_OPENED, 431, ISSUE_OPENED:431", "ISSUE_CLOSED, 431, ISSUE_CLOSED:431")
    fun `given event, when reading id, then it is type and entity ref joined by a colon`(
        type: NarrativeEventType,
        ref: String,
        expectedId: String,
    ) {
        // When
        val event =
            NarrativeEvent(
                type = type,
                entity = NarrativeEntityRef(source = type.source, ref = ref),
                timestamp = timestamp,
                title = "title",
            )

        // Then
        assertThat(event.id).isEqualTo(expectedId)
    }
}
