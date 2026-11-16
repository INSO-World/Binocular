package com.inso_world.binocular.core.service.narrative

import com.inso_world.binocular.core.unit.base.BaseUnitTest
import com.inso_world.binocular.model.narrative.NarrativeEvent
import com.inso_world.binocular.model.narrative.NarrativeEventType
import com.inso_world.binocular.model.narrative.NarrativeGranularity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDateTime

/**
 * BDD tests for [NarrativeService].
 * The port is replaced by an in-memory fake, so the tests cover only how raw activity is turned into
 * events, correlated and segmented.
 */
internal class NarrativeServiceTest : BaseUnitTest() {
    /** In-memory port that returns fixed activity and records the period it was asked for. */
    private class FakeNarrativePort(
        private val commits: List<CommitActivity> = emptyList(),
        private val issues: List<IssueActivity> = emptyList(),
        private val builds: List<BuildActivity> = emptyList(),
    ) : NarrativeInfrastructurePort {
        val requestedPeriods = mutableListOf<Pair<LocalDateTime?, LocalDateTime?>>()

        override fun findCommitActivities(
            since: LocalDateTime?,
            until: LocalDateTime?,
        ) = commits.also { requestedPeriods += since to until }

        override fun findIssueActivities(
            since: LocalDateTime?,
            until: LocalDateTime?,
        ) = issues.also { requestedPeriods += since to until }

        override fun findBuildActivities(
            since: LocalDateTime?,
            until: LocalDateTime?,
        ) = builds.also { requestedPeriods += since to until }
    }

    /** Friday, 14 March 2025, 10:00; the week of this day starts on Monday, 10 March. */
    private val friday = LocalDateTime.of(2025, 3, 14, 10, 0)

    private fun commit(
        sha: String,
        timestamp: LocalDateTime = friday,
        message: String = "message of $sha",
        author: String? = "Jane Doe",
        parentCount: Int = 1,
        filePaths: List<String> = emptyList(),
        issueNumbers: List<Int> = emptyList(),
    ) = CommitActivity(sha, timestamp, message, author, parentCount, filePaths, issueNumbers, webUrl = null)

    private fun issue(
        number: Int,
        createdAt: LocalDateTime = friday,
        closedAt: LocalDateTime? = null,
    ) = IssueActivity(number, "issue $number", createdAt, closedAt, author = "Jane Doe", webUrl = null)

    private fun build(
        id: String,
        timestamp: LocalDateTime = friday,
        status: String? = "success",
        title: String? = "build $id",
        commitShas: List<String> = emptyList(),
    ) = BuildActivity(id, status, timestamp, title, actor = "janedoe", commitShas = commitShas, webUrl = null)

    private fun compose(
        commits: List<CommitActivity> = emptyList(),
        issues: List<IssueActivity> = emptyList(),
        builds: List<BuildActivity> = emptyList(),
        granularity: NarrativeGranularity = NarrativeGranularity.MONTH,
        since: LocalDateTime? = null,
        until: LocalDateTime? = null,
    ) = NarrativeService(FakeNarrativePort(commits, issues, builds)).compose(granularity, since, until)

    private fun events(
        commits: List<CommitActivity> = emptyList(),
        issues: List<IssueActivity> = emptyList(),
        builds: List<BuildActivity> = emptyList(),
        since: LocalDateTime? = null,
        until: LocalDateTime? = null,
    ): List<NarrativeEvent> = compose(commits, issues, builds, since = since, until = until).flatMap { it.events }

    private fun List<NarrativeEvent>.withId(id: String) = single { it.id == id }

    private fun NarrativeEvent.relatedKeys() = relatedEntities.map { it.key }

    @Nested
    inner class Events {
        /** One parent makes a commit, two parents a merge; the title is the first line of the message. */
        @Test
        fun `given commits with one and two parents, when composing, then they become commit and merge events`() {
            // When
            val events =
                events(
                    commits =
                        listOf(
                            commit("a1", message = "Add resolver\n\nDetails", parentCount = 1),
                            commit("b2", parentCount = 2),
                        ),
                )

            // Then
            assertAll(
                { assertThat(events.withId("COMMIT:a1").title).isEqualTo("Add resolver") },
                { assertThat(events.withId("COMMIT:a1").actor).isEqualTo("Jane Doe") },
                { assertThat(events.withId("MERGE:b2").type).isEqualTo(NarrativeEventType.MERGE) },
            )
        }

        /** A file's module is its top-level directory; root files share one module; duplicates collapse. */
        @Test
        fun `given changed files, when composing, then modules are the distinct top-level directories`() {
            // When
            val event =
                events(commits = listOf(commit("a1", filePaths = listOf("web/src/A.kt", "web/pom.xml", "README.md", "core/B.kt"))))
                    .single()

            // Then
            assertThat(event.modules).containsExactly(NarrativeService.ROOT_MODULE, "core", "web")
        }

        /** A closed issue yields an opened event by its author and a closed event without an actor. */
        @Test
        fun `given closed issue, when composing, then it becomes an opened and a closed event`() {
            // When
            val events = events(issues = listOf(issue(431, createdAt = friday, closedAt = friday.plusDays(2))))

            // Then
            assertAll(
                { assertThat(events.map { it.id }).containsExactly("ISSUE_OPENED:431", "ISSUE_CLOSED:431") },
                { assertThat(events[0].actor).isEqualTo("Jane Doe") },
                { assertThat(events[1].actor).isNull() },
                { assertThat(events[1].timestamp).isEqualTo(friday.plusDays(2)) },
                { assertThat(events[1].title).isEqualTo("issue 431") },
            )
        }

        /** An issue opened before the period but closed within it contributes only its closed event. */
        @Test
        fun `given issue opened before the period, when composing, then only the closed event is included`() {
            // When
            val events =
                events(
                    issues = listOf(issue(431, createdAt = friday.minusMonths(2), closedAt = friday)),
                    since = friday.minusDays(1),
                    until = friday.plusDays(1),
                )

            // Then
            assertThat(events.map { it.id }).containsExactly("ISSUE_CLOSED:431")
        }

        /** Only statuses that say whether the build worked produce an event; the comparison ignores case. */
        @ParameterizedTest
        @CsvSource(
            "success, BUILD_SUCCEEDED",
            "SUCCESS, BUILD_SUCCEEDED",
            "failed, BUILD_FAILED",
            "failure, BUILD_FAILED",
            "startup_failure, BUILD_FAILED",
            "cancelled,",
            "skipped,",
            ",",
        )
        fun `given build status, when composing, then only success and failure produce an event`(
            status: String?,
            expectedType: NarrativeEventType?,
        ) {
            // When
            val events = events(builds = listOf(build("982", status = status)))

            // Then
            assertThat(events.map { it.type }).isEqualTo(listOfNotNull(expectedType))
        }

        /** A build without a title is titled with its id so that every event has a readable title. */
        @Test
        fun `given build without title, when composing, then its title names the build id`() {
            // When
            val events = events(builds = listOf(build("982", title = null), build("983", title = " ")))

            // Then
            assertThat(events.map { it.title }).containsExactly("Build 982", "Build 983")
        }
    }

    @Nested
    inner class Correlation {
        /** Stored links are visible from both ends: commit to issue and build, and back. */
        @Test
        fun `given stored links, when composing, then commit, issue and build reference each other`() {
            // When
            val events =
                events(
                    commits = listOf(commit("a1", issueNumbers = listOf(431))),
                    issues = listOf(issue(431, closedAt = friday.plusDays(1))),
                    builds = listOf(build("982", timestamp = friday.plusHours(5), commitShas = listOf("a1"))),
                )

            // Then
            assertAll(
                { assertThat(events.withId("COMMIT:a1").relatedKeys()).containsExactly("ITS:431", "CI:982") },
                { assertThat(events.withId("ISSUE_OPENED:431").relatedKeys()).containsExactly("VCS:a1") },
                { assertThat(events.withId("ISSUE_CLOSED:431").relatedKeys()).containsExactly("VCS:a1") },
                { assertThat(events.withId("BUILD_SUCCEEDED:982").relatedKeys()).containsExactly("VCS:a1") },
            )
        }

        /** Without a stored link, a build belongs to the latest earlier commit if it is close enough in time. */
        @Test
        fun `given unlinked build shortly after commits, when composing, then it is linked to the latest earlier commit`() {
            // When
            val events =
                events(
                    commits =
                        listOf(
                            commit("a1", timestamp = friday.minusMinutes(8)),
                            commit("b2", timestamp = friday.minusMinutes(3)),
                            commit("c3", timestamp = friday.plusMinutes(1)),
                        ),
                    builds = listOf(build("982", timestamp = friday)),
                )

            // Then
            assertAll(
                { assertThat(events.withId("BUILD_SUCCEEDED:982").relatedKeys()).containsExactly("VCS:b2") },
                { assertThat(events.withId("COMMIT:b2").relatedKeys()).containsExactly("CI:982") },
                { assertThat(events.withId("COMMIT:a1").relatedKeys()).isEmpty() },
                { assertThat(events.withId("COMMIT:c3").relatedKeys()).isEmpty() },
            )
        }

        /** The proximity window is inclusive at exactly ten minutes and closed one second later. */
        @Test
        fun `given unlinked builds at and beyond the proximity limit, when composing, then only the one at the limit is linked`() {
            // When
            val events =
                events(
                    commits = listOf(commit("a1", timestamp = friday)),
                    builds =
                        listOf(
                            build("982", timestamp = friday.plus(NarrativeService.BUILD_PROXIMITY)),
                            build("983", timestamp = friday.plus(NarrativeService.BUILD_PROXIMITY).plusSeconds(1)),
                        ),
                )

            // Then
            assertAll(
                { assertThat(events.withId("BUILD_SUCCEEDED:982").relatedKeys()).containsExactly("VCS:a1") },
                { assertThat(events.withId("BUILD_SUCCEEDED:983").relatedKeys()).isEmpty() },
            )
        }

        /** A stored link is a fact and is never replaced by a closer commit. */
        @Test
        fun `given linked build near another commit, when composing, then the stored link wins`() {
            // When
            val events =
                events(
                    commits = listOf(commit("a1", timestamp = friday.minusDays(3)), commit("b2", timestamp = friday.minusMinutes(1))),
                    builds = listOf(build("982", timestamp = friday, commitShas = listOf("a1"))),
                )

            // Then
            assertThat(events.withId("BUILD_SUCCEEDED:982").relatedKeys()).containsExactly("VCS:a1")
        }

        /** Issues and builds carry the modules of their commits so that a module filter finds them too. */
        @Test
        fun `given linked commit with modules, when composing, then issue and build inherit them`() {
            // When
            val events =
                events(
                    commits = listOf(commit("a1", filePaths = listOf("web/A.kt", "core/B.kt"), issueNumbers = listOf(431))),
                    issues = listOf(issue(431)),
                    builds = listOf(build("982", commitShas = listOf("a1")), build("983", timestamp = friday.plusDays(1))),
                )

            // Then
            assertAll(
                { assertThat(events.withId("ISSUE_OPENED:431").modules).containsExactly("core", "web") },
                { assertThat(events.withId("BUILD_SUCCEEDED:982").modules).containsExactly("core", "web") },
                { assertThat(events.withId("BUILD_SUCCEEDED:983").modules).isEmpty() },
            )
        }
    }

    @Nested
    inner class Segments {
        /** Months without activity between the first and the last event still appear, as empty segments. */
        @Test
        fun `given events in January and March, when composing by month, then February is an empty segment`() {
            // When
            val segments =
                compose(
                    commits =
                        listOf(
                            commit("a1", timestamp = LocalDateTime.of(2025, 1, 31, 23, 59)),
                            commit("b2", timestamp = LocalDateTime.of(2025, 3, 1, 0, 0)),
                        ),
                )

            // Then
            assertAll(
                {
                    assertThat(segments.map { it.start }).containsExactly(
                        LocalDateTime.of(2025, 1, 1, 0, 0),
                        LocalDateTime.of(2025, 2, 1, 0, 0),
                        LocalDateTime.of(2025, 3, 1, 0, 0),
                    )
                },
                { assertThat(segments.map { it.end }).isEqualTo(segments.drop(1).map { it.start } + LocalDateTime.of(2025, 4, 1, 0, 0)) },
                { assertThat(segments.map { it.events.size }).containsExactly(1, 0, 1) },
                { assertThat(segments.map { it.granularity }).containsOnly(NarrativeGranularity.MONTH) },
            )
        }

        /** A week runs from Monday 00:00 to the next Monday: Sunday night and Monday morning are split. */
        @Test
        fun `given events on Sunday and Monday, when composing by week, then they fall into consecutive weeks`() {
            // When
            val segments =
                compose(
                    commits =
                        listOf(
                            commit("a1", timestamp = LocalDateTime.of(2025, 3, 16, 23, 59)),
                            commit("b2", timestamp = LocalDateTime.of(2025, 3, 17, 0, 0)),
                        ),
                    granularity = NarrativeGranularity.WEEK,
                )

            // Then
            assertAll(
                {
                    assertThat(segments.map { it.start })
                        .containsExactly(LocalDateTime.of(2025, 3, 10, 0, 0), LocalDateTime.of(2025, 3, 17, 0, 0))
                },
                { assertThat(segments.last().end).isEqualTo(LocalDateTime.of(2025, 3, 24, 0, 0)) },
                { assertThat(segments.map { it.events.single().id }).containsExactly("COMMIT:a1", "COMMIT:b2") },
            )
        }

        /** Events are ordered by time; at the same moment a commit precedes its build. */
        @Test
        fun `given unordered activity, when composing, then events are in chronological order`() {
            // When
            val events =
                events(
                    commits = listOf(commit("a1", timestamp = friday)),
                    issues = listOf(issue(431, createdAt = friday.minusHours(1))),
                    builds = listOf(build("982", timestamp = friday, commitShas = listOf("a1"))),
                )

            // Then
            assertThat(events.map { it.id }).containsExactly("ISSUE_OPENED:431", "COMMIT:a1", "BUILD_SUCCEEDED:982")
        }

        /** The top module is the one most commits touched; a tie goes to the alphabetically first module. */
        @Test
        fun `given commits in several modules, when composing, then top module is the most touched one`() {
            // When
            val busy =
                compose(
                    commits =
                        listOf(
                            commit("a1", filePaths = listOf("web/A.kt")),
                            commit("b2", filePaths = listOf("web/B.kt", "core/C.kt")),
                        ),
                ).single()
            val tied = compose(commits = listOf(commit("a1", filePaths = listOf("web/A.kt", "core/C.kt")))).single()
            val withoutFiles = compose(commits = listOf(commit("a1"))).single()

            // Then
            assertAll(
                { assertThat(busy.topModule).isEqualTo("web") },
                { assertThat(tied.topModule).isEqualTo("core") },
                { assertThat(withoutFiles.topModule).isNull() },
            )
        }

        /** Every segment is summarised from its own events, including the empty one in between. */
        @Test
        fun `given segments with and without events, when composing, then each carries its summary`() {
            // When
            val segments =
                compose(
                    commits =
                        listOf(
                            commit("a1", timestamp = LocalDateTime.of(2025, 1, 15, 9, 0), filePaths = listOf("web/A.kt")),
                            commit("b2", timestamp = LocalDateTime.of(2025, 3, 15, 9, 0), parentCount = 2),
                        ),
                )

            // Then
            assertThat(segments.map { it.summary }).containsExactly(
                "1 commit by 1 contributor, mostly in web.",
                NarrativeSummarizer.NO_ACTIVITY,
                "1 merge by 1 contributor.",
            )
        }

        /** The same entity delivered twice by the port must not appear twice in the narrative. */
        @Test
        fun `given the same commit twice, when composing, then it yields one event`() {
            // When
            val events = events(commits = listOf(commit("a1"), commit("a1")))

            // Then
            assertThat(events.map { it.id }).containsExactly("COMMIT:a1")
        }

        /** Without any activity there is no narrative, and the requested period is passed on to the port. */
        @Test
        fun `given no activity, when composing a period, then result is empty and the port got that period`() {
            // Given
            val port = FakeNarrativePort()
            val since = friday.minusDays(1)
            val until = friday.plusDays(1)

            // When
            val segments = NarrativeService(port).compose(NarrativeGranularity.WEEK, since, until)

            // Then
            assertAll(
                { assertThat(segments).isEmpty() },
                { assertThat(port.requestedPeriods).containsOnly(since to until).hasSize(3) },
            )
        }
    }
}
