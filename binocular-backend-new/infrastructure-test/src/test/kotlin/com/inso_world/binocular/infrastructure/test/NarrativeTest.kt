package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.core.service.narrative.NarrativeInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.DisabledIf
import java.time.LocalDateTime

/**
 * Contract tests for [NarrativeInfrastructurePort] against the seeded [TestDataProvider] data.
 * The port is only implemented by the ArangoDB adapter, so the tests are skipped under the postgres profile.
 */
@DisabledIf(expression = "#{environment['spring.profiles.active'].contains('postgres')}", loadContext = true)
internal class NarrativeTest : BaseInfrastructureSpringTest() {
    @Autowired
    lateinit var narrativePort: NarrativeInfrastructurePort

    private val commit1 = TestDataProvider.testCommits[0]
    private val commit2 = TestDataProvider.testCommits[1]

    /** Commit 1 is seeded with a file, an issue and no parent; its stored relations must all be returned. */
    @Test
    fun `commit activity carries author, files and linked issues`() {
        // When
        val commit = narrativePort.findCommitActivities(since = null, until = null).single { it.sha == commit1.sha }

        // Then
        assertAll(
            { assertThat(commit.message).isEqualTo(commit1.message) },
            { assertThat(commit.author).isEqualTo(commit1.author.name) },
            { assertThat(commit.parentCount).isZero() },
            { assertThat(commit.filePaths).containsExactly(TestDataProvider.testFiles[0].path) },
            { assertThat(commit.issueNumbers).containsExactly(TestDataProvider.testIssues[0].platformIid) },
            { assertThat(commit.webUrl).isEqualTo(commit1.webUrl) },
        )
    }

    /** Commit 2 is seeded as child of commit 1 and has no other relations. */
    @Test
    fun `commit activity counts parents and leaves missing relations empty`() {
        // When
        val commit = narrativePort.findCommitActivities(since = null, until = null).single { it.sha == commit2.sha }

        // Then
        assertAll(
            { assertThat(commit.parentCount).isEqualTo(1) },
            { assertThat(commit.filePaths).isEmpty() },
            { assertThat(commit.issueNumbers).isEmpty() },
        )
    }

    /** Both seeded issues are returned with their platform number, title and timestamps. */
    @Test
    fun `issue activities carry number, title and timestamps`() {
        // When
        val issues = narrativePort.findIssueActivities(since = null, until = null).associateBy { it.number }

        // Then
        assertAll(
            { assertThat(issues.keys).containsExactlyInAnyOrder(101, 102) },
            { assertThat(issues.getValue(101).title).isEqualTo(TestDataProvider.testIssues[0].title) },
            { assertThat(issues.getValue(102).closedAt).isNotNull() },
            { assertThat(issues.getValue(102).webUrl).isEqualTo(TestDataProvider.testIssues[1].webUrl) },
        )
    }

    /** Build 1 is seeded for commit 1, build 2 has no commit link. */
    @Test
    fun `build activities carry status, actor and linked commits`() {
        // When
        val builds = narrativePort.findBuildActivities(since = null, until = null).associateBy { it.id }

        // Then
        assertAll(
            { assertThat(builds.keys).containsExactlyInAnyOrder("1", "2") },
            { assertThat(builds.getValue("1").status).isEqualTo("success") },
            { assertThat(builds.getValue("1").actor).isEqualTo("User One") },
            { assertThat(builds.getValue("1").commitShas).containsExactly(commit1.sha) },
            { assertThat(builds.getValue("2").status).isEqualTo("failed") },
            { assertThat(builds.getValue("2").commitShas).isEmpty() },
        )
    }

    /**
     * All seeded activity is stamped with the current time, so a period around now contains it and
     * periods that end before or start after it are empty.
     */
    @Test
    fun `period bounds filter commits, issues and builds`() {
        // Given
        val yesterday = LocalDateTime.now().minusDays(1)
        val tomorrow = LocalDateTime.now().plusDays(1)

        // Then
        assertAll(
            { assertThat(narrativePort.findCommitActivities(since = yesterday, until = tomorrow)).hasSize(4) },
            { assertThat(narrativePort.findIssueActivities(since = yesterday, until = tomorrow)).hasSize(2) },
            { assertThat(narrativePort.findBuildActivities(since = yesterday, until = tomorrow)).hasSize(2) },
            { assertThat(narrativePort.findCommitActivities(since = null, until = yesterday)).isEmpty() },
            { assertThat(narrativePort.findIssueActivities(since = null, until = yesterday)).isEmpty() },
            { assertThat(narrativePort.findBuildActivities(since = null, until = yesterday)).isEmpty() },
            { assertThat(narrativePort.findCommitActivities(since = tomorrow, until = null)).isEmpty() },
            { assertThat(narrativePort.findIssueActivities(since = tomorrow, until = null)).isEmpty() },
            { assertThat(narrativePort.findBuildActivities(since = tomorrow, until = null)).isEmpty() },
        )
    }
}
