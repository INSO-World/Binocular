package com.inso_world.binocular.infrastructure.arangodb.service

import com.arangodb.ArangoCursor
import com.arangodb.springframework.core.ArangoOperations
import com.inso_world.binocular.core.service.narrative.BuildActivity
import com.inso_world.binocular.core.service.narrative.CommitActivity
import com.inso_world.binocular.core.service.narrative.IssueActivity
import com.inso_world.binocular.core.unit.base.BaseUnitTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.LocalDateTime

/**
 * Unit tests for [NarrativeInfrastructurePortImpl] with a mocked [ArangoOperations].
 * They cover how query rows are converted into activity records and how a period is bound to a query;
 * that the queries themselves run against ArangoDB is covered by the contract test in `infrastructure-test`.
 */
internal class NarrativeInfrastructurePortImplTest : BaseUnitTest() {
    private val arangoOperations = mockk<ArangoOperations>()
    private val port = NarrativeInfrastructurePortImpl(arangoOperations)

    /** 2025-03-14T10:30:00Z as epoch milliseconds, the unit the queries return timestamps in. */
    private val timestampMillis = 1741948200000L
    private val timestamp = LocalDateTime.of(2025, 3, 14, 10, 30)

    /**
     * Lets the mocked template answer every query with the given rows and captures the bind variables.
     */
    private fun givenRows(vararg rows: Map<String, Any?>): () -> Map<String, Any?> {
        val bindVars = slot<Map<String, Any?>>()
        val cursor = mockk<ArangoCursor<Map<*, *>>>()
        every { cursor.asListRemaining() } returns rows.toList()
        every { arangoOperations.query(any<String>(), capture(bindVars), Map::class.java) } returns cursor
        return { bindVars.captured }
    }

    /** A complete commit row is converted field by field; numbers arrive as generic JSON numbers. */
    @Test
    fun `given complete commit row, when finding commits, then all fields are converted`() {
        // Given
        givenRows(
            mapOf(
                "sha" to "a1b2c3d4",
                "timestamp" to timestampMillis,
                "message" to "#431: Add resolver",
                "author" to "Jane Doe",
                "parentCount" to 2L,
                "filePaths" to listOf("web/Resolver.kt", "web/Schema.kt"),
                "issueNumbers" to listOf(431L, 7),
                "webUrl" to "https://example.com/commit/a1b2c3d4",
            ),
        )

        // When
        val commits = port.findCommitActivities(since = null, until = null)

        // Then
        assertThat(commits).containsExactly(
            CommitActivity(
                sha = "a1b2c3d4",
                timestamp = timestamp,
                message = "#431: Add resolver",
                author = "Jane Doe",
                parentCount = 2,
                filePaths = listOf("web/Resolver.kt", "web/Schema.kt"),
                issueNumbers = listOf(431, 7),
                webUrl = "https://example.com/commit/a1b2c3d4",
            ),
        )
    }

    /** Attributes missing in the database become empty values instead of failing the whole narrative. */
    @Test
    fun `given commit row with missing optional values, when finding commits, then they are empty`() {
        // Given
        givenRows(
            mapOf(
                "sha" to "a1b2c3d4",
                "timestamp" to timestampMillis,
                "message" to null,
                "author" to null,
                "parentCount" to 0,
                "filePaths" to null,
                "issueNumbers" to null,
                "webUrl" to null,
            ),
        )

        // When
        val commit = port.findCommitActivities(since = null, until = null).single()

        // Then
        assertAll(
            { assertThat(commit.message).isEmpty() },
            { assertThat(commit.author).isNull() },
            { assertThat(commit.parentCount).isZero() },
            { assertThat(commit.filePaths).isEmpty() },
            { assertThat(commit.issueNumbers).isEmpty() },
            { assertThat(commit.webUrl).isNull() },
        )
    }

    /** An open issue has no closing time, a closed one has both timestamps converted to UTC date-times. */
    @Test
    fun `given open and closed issue rows, when finding issues, then closedAt is null only for the open one`() {
        // Given
        givenRows(
            mapOf(
                "number" to 431L,
                "title" to "GraphQL solver",
                "createdAt" to timestampMillis,
                "closedAt" to timestampMillis + 60_000,
                "author" to "Jane Doe",
                "webUrl" to "https://example.com/issues/431",
            ),
            mapOf(
                "number" to 432,
                "title" to null,
                "createdAt" to timestampMillis,
                "closedAt" to null,
                "author" to null,
                "webUrl" to null,
            ),
        )

        // When
        val issues = port.findIssueActivities(since = null, until = null)

        // Then
        assertThat(issues).containsExactly(
            IssueActivity(
                number = 431,
                title = "GraphQL solver",
                createdAt = timestamp,
                closedAt = timestamp.plusMinutes(1),
                author = "Jane Doe",
                webUrl = "https://example.com/issues/431",
            ),
            IssueActivity(number = 432, title = "", createdAt = timestamp, closedAt = null, author = null, webUrl = null),
        )
    }

    /** A build row is converted field by field; a build without status, title or links stays valid. */
    @Test
    fun `given linked and unlinked build rows, when finding builds, then all fields are converted`() {
        // Given
        givenRows(
            mapOf(
                "id" to "982",
                "status" to "failed",
                "timestamp" to timestampMillis,
                "title" to "#431: Add resolver",
                "actor" to "janedoe",
                "commitShas" to listOf("a1b2c3d4"),
                "webUrl" to "https://example.com/runs/982",
            ),
            mapOf(
                "id" to "983",
                "status" to null,
                "timestamp" to timestampMillis,
                "title" to null,
                "actor" to null,
                "commitShas" to null,
                "webUrl" to null,
            ),
        )

        // When
        val builds = port.findBuildActivities(since = null, until = null)

        // Then
        assertThat(builds).containsExactly(
            BuildActivity(
                id = "982",
                status = "failed",
                timestamp = timestamp,
                title = "#431: Add resolver",
                actor = "janedoe",
                commitShas = listOf("a1b2c3d4"),
                webUrl = "https://example.com/runs/982",
            ),
            BuildActivity(
                id = "983",
                status = null,
                timestamp = timestamp,
                title = null,
                actor = null,
                commitShas = emptyList(),
                webUrl = null,
            ),
        )
    }

    /** The period is handed to the query as UTC epoch milliseconds, which is what the AQL compares in. */
    @Test
    fun `given period, when finding activities, then bounds are bound as epoch milliseconds`() {
        // Given
        val bindVars = givenRows()

        // When
        port.findCommitActivities(since = timestamp, until = timestamp.plusMinutes(1))

        // Then
        assertThat(bindVars()).isEqualTo(mapOf("since" to timestampMillis, "until" to timestampMillis + 60_000))
    }

    /** An open period is bound as null on both sides, so the query selects the whole history. */
    @Test
    fun `given no period, when finding activities, then bounds are bound as null`() {
        // Given
        val bindVars = givenRows()

        // When
        val builds = port.findBuildActivities(since = null, until = null)

        // Then
        assertAll(
            { assertThat(builds).isEmpty() },
            { assertThat(bindVars()).isEqualTo(mapOf("since" to null, "until" to null)) },
        )
    }
}
