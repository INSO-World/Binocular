package com.inso_world.binocular.web.graphql.controller

import com.fasterxml.jackson.databind.JsonNode
import com.inso_world.binocular.core.integration.base.BaseIntegrationTest
import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.web.graphql.base.GraphQlControllerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.DayOfWeek
import java.time.OffsetDateTime

/**
 * Tests the root query `narrative` through the GraphQL transport against the seeded [TestDataProvider]
 * data: four commits, two issues and two builds (one succeeded and linked to the first commit, one failed).
 */
internal class NarrativeControllerWebTest : BaseIntegrationTest() {
    @Nested
    inner class Narrative : GraphQlControllerTest() {
        /** Runs the given `narrative(...)` selection with all fields and returns the segments. */
        private fun narrative(arguments: String): List<JsonNode> =
            graphQlTester
                .document(
                    """
                    query {
                        narrative($arguments) {
                            granularity
                            start
                            end
                            summary
                            topModule
                            actors
                            commitCount
                            mergeCount
                            issuesOpened
                            issuesClosed
                            buildsSucceeded
                            buildsFailed
                            events {
                                id
                                type
                                source
                                entityKey
                                timestamp
                                title
                                actor
                                modules
                                relatedEntityKeys
                                webUrl
                            }
                        }
                    }
                    """,
                ).execute()
                .path("narrative")
                .entityList(JsonNode::class.java)
                .get()

        /**
         * Without an upper bound the whole seeded history is returned; the key figures of the segments
         * add up to the seeded commits, issues and builds, and the stored links connect the events.
         */
        @Test
        fun `given seeded data, when querying by month, then all activity is returned and correlated`() {
            // Given
            val commitSha = TestDataProvider.testCommits[0].sha

            // When
            val segments = narrative("""granularity: MONTH, since: "2000-01-01T00:00:00Z"""")

            // Then
            val events = segments.flatMap { it.get("events").toList() }
            val commit = events.single { it.get("entityKey").asText() == "VCS:$commitSha" }
            val build = events.single { it.get("entityKey").asText() == "CI:1" }
            assertAll(
                { assertThat(segments).allMatch { it.get("granularity").asText() == "MONTH" } },
                { assertThat(segments.sumOf { it.get("commitCount").asInt() + it.get("mergeCount").asInt() }).isEqualTo(4) },
                { assertThat(segments.sumOf { it.get("issuesOpened").asInt() }).isEqualTo(2) },
                { assertThat(segments.sumOf { it.get("buildsSucceeded").asInt() }).isEqualTo(1) },
                { assertThat(segments.sumOf { it.get("buildsFailed").asInt() }).isEqualTo(1) },
                { assertThat(segments.last().get("summary").asText()).contains("commit") },
                { assertThat(commit.get("source").asText()).isEqualTo("VCS") },
                { assertThat(commit.get("relatedEntityKeys").map { it.asText() }).contains("ITS:101", "CI:1") },
                { assertThat(build.get("type").asText()).isEqualTo("BUILD_SUCCEEDED") },
                { assertThat(build.get("relatedEntityKeys").map { it.asText() }).containsExactly("VCS:$commitSha") },
            )
        }

        /** The seeded activity is stamped with the current time, so a period that ended in 1970 is empty. */
        @Test
        fun `given a period before all activity, when querying, then no segments are returned`() {
            // When
            val segments = narrative("granularity: MONTH, until: 1000")

            // Then
            assertThat(segments).isEmpty()
        }

        /** A period that ends before or when it starts is rejected, so that swapped arguments are noticed. */
        @ParameterizedTest
        @CsvSource("2000, 1000", "1000, 1000")
        fun `given since not before until, when querying, then a validation error is returned`(
            since: Long,
            until: Long,
        ) {
            // When
            val response =
                graphQlTester
                    .document("query { narrative(granularity: MONTH, since: $since, until: $until) { start } }")
                    .execute()

            // Then
            response
                .errors()
                .expect { it.message == "Since must be before until" && it.extensions["code"] == "VALIDATION_ERROR" }
                .verify()
        }

        /** Weekly segments start on a Monday at midnight UTC and last seven days. */
        @Test
        fun `given seeded data, when querying by week, then segments span Monday to Monday`() {
            // When
            val segments = narrative("granularity: WEEK")

            // Then
            assertThat(segments).isNotEmpty()
            segments.forEach { segment ->
                val start = OffsetDateTime.parse(segment.get("start").asText())
                val end = OffsetDateTime.parse(segment.get("end").asText())
                assertAll(
                    { assertThat(segment.get("granularity").asText()).isEqualTo("WEEK") },
                    { assertThat(start.dayOfWeek).isEqualTo(DayOfWeek.MONDAY) },
                    { assertThat(start.toLocalTime().toSecondOfDay()).isZero() },
                    { assertThat(end).isEqualTo(start.plusWeeks(1)) },
                )
            }
        }
    }
}
