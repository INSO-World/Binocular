package com.inso_world.binocular.core.service.narrative

import com.inso_world.binocular.core.unit.base.BaseUnitTest
import com.inso_world.binocular.model.narrative.NarrativeEntityRef
import com.inso_world.binocular.model.narrative.NarrativeEvent
import com.inso_world.binocular.model.narrative.NarrativeEventType
import com.inso_world.binocular.model.narrative.NarrativeGranularity
import com.inso_world.binocular.model.narrative.NarrativeSegment
import com.inso_world.binocular.model.narrative.NarrativeSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDateTime

/**
 * BDD tests for [NarrativeSummarizer].
 * Each test builds a segment with a given number of events per type and checks the sentence templates.
 */
internal class NarrativeSummarizerTest : BaseUnitTest() {
    private val start = LocalDateTime.of(2025, 3, 1, 0, 0)

    /**
     * Summarises a segment with the given number of events per type.
     * Commits and merges are spread over [contributors] authors; issue and build events have another actor.
     */
    private fun summary(
        commits: Int = 0,
        merges: Int = 0,
        opened: Int = 0,
        closed: Int = 0,
        succeeded: Int = 0,
        failed: Int = 0,
        contributors: Int = 0,
        topModule: String? = null,
    ): String {
        val types =
            List(commits) { NarrativeEventType.COMMIT } +
                List(merges) { NarrativeEventType.MERGE } +
                List(opened) { NarrativeEventType.ISSUE_OPENED } +
                List(closed) { NarrativeEventType.ISSUE_CLOSED } +
                List(succeeded) { NarrativeEventType.BUILD_SUCCEEDED } +
                List(failed) { NarrativeEventType.BUILD_FAILED }
        val events =
            types.mapIndexed { index, type ->
                NarrativeEvent(
                    type = type,
                    entity = NarrativeEntityRef(type.source, "$type-$index"),
                    timestamp = start.plusMinutes(index.toLong()),
                    title = "title",
                    actor =
                        when {
                            type.source != NarrativeSource.VCS -> "Someone Else"
                            contributors > 0 -> "Developer ${index % contributors}"
                            else -> null
                        },
                )
            }
        val segment = NarrativeSegment(NarrativeGranularity.MONTH, start, start.plusMonths(1), events, topModule = topModule)
        return NarrativeSummarizer().summarize(segment)
    }

    /** A period without events is stated as such instead of yielding an empty text. */
    @Test
    fun `given no events, when summarizing, then summary states no activity`() {
        // Then
        assertThat(summary()).isEqualTo(NarrativeSummarizer.NO_ACTIVITY)
    }

    /** Commits and merges are counted with nouns in the matching number; absent parts are left out. */
    @ParameterizedTest
    @CsvSource(
        delimiter = '|',
        value = [
            "1 | 0 | 1 commit.",
            "2 | 0 | 2 commits.",
            "0 | 1 | 1 merge.",
            "0 | 3 | 3 merges.",
            "52 | 3 | 52 commits and 3 merges.",
        ],
    )
    fun `given commits and merges, when summarizing, then they are counted in the matching number`(
        commits: Int,
        merges: Int,
        expected: String,
    ) {
        // Then
        assertThat(summary(commits = commits, merges = merges)).isEqualTo(expected)
    }

    /** Contributors are the distinct authors of commits and merges; the top module is named after them. */
    @Test
    fun `given authors and a top module, when summarizing, then both are appended to the commit sentence`() {
        // Then
        assertThat(summary(commits = 5, merges = 1, contributors = 3, topModule = "ui"))
            .isEqualTo("5 commits and 1 merge by 3 contributors, mostly in ui.")
    }

    /** The technical name of the root module is replaced by readable wording. */
    @Test
    fun `given root module as top module, when summarizing, then it is written out`() {
        // Then
        assertThat(summary(commits = 2, topModule = NarrativeService.ROOT_MODULE))
            .isEqualTo("2 commits, mostly in the repository root.")
    }

    /** A single author is written in the singular. */
    @Test
    fun `given one author, when summarizing, then contributor is singular`() {
        // Then
        assertThat(summary(commits = 4, contributors = 1)).isEqualTo("4 commits by 1 contributor.")
    }

    /** Opened and closed issues share one sentence; the noun is not repeated for the closed ones. */
    @ParameterizedTest
    @CsvSource(
        delimiter = '|',
        value = [
            "4 | 1 | 4 issues opened, 1 closed.",
            "1 | 3 | 1 issue opened, 3 closed.",
            "1 | 0 | 1 issue opened.",
            "0 | 2 | 2 issues closed.",
        ],
    )
    fun `given opened and closed issues, when summarizing, then they share one sentence`(
        opened: Int,
        closed: Int,
        expected: String,
    ) {
        // Then
        assertThat(summary(opened = opened, closed = closed)).isEqualTo(expected)
    }

    /** Builds are summarised by their failures; uniform results and a single build get their own wording. */
    @ParameterizedTest
    @CsvSource(
        delimiter = '|',
        value = [
            "1 | 0 | 1 build succeeded.",
            "0 | 1 | 1 build failed.",
            "12 | 0 | All 12 builds succeeded.",
            "0 | 3 | All 3 builds failed.",
            "9 | 3 | 3 of 12 builds failed.",
            "1 | 1 | 1 of 2 builds failed.",
        ],
    )
    fun `given build results, when summarizing, then failures are put in relation to all builds`(
        succeeded: Int,
        failed: Int,
        expected: String,
    ) {
        // Then
        assertThat(summary(succeeded = succeeded, failed = failed)).isEqualTo(expected)
    }

    /**
     * The sentences follow the order version control, issues, builds, and actors of issue and build
     * events do not count as contributors.
     */
    @Test
    fun `given events of all sources, when summarizing, then one sentence per source is joined in order`() {
        // When
        val summary =
            summary(
                commits = 52,
                merges = 3,
                opened = 4,
                closed = 1,
                succeeded = 50,
                failed = 17,
                contributors = 6,
                topModule = "binocular-frontend-new",
            )

        // Then
        assertThat(summary).isEqualTo(
            "52 commits and 3 merges by 6 contributors, mostly in binocular-frontend-new. " +
                "4 issues opened, 1 closed. 17 of 67 builds failed.",
        )
    }
}
