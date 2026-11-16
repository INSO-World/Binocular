package com.inso_world.binocular.core.service.narrative

import com.inso_world.binocular.model.narrative.NarrativeEventType
import com.inso_world.binocular.model.narrative.NarrativeSegment
import com.inso_world.binocular.model.narrative.NarrativeSource

/**
 * Writes the textual micro-summary of a narrative segment from fixed sentence templates.
 *
 * ## Purpose
 * Gives every [NarrativeSegment] a short description of its key activities that can be read without
 * looking at the single events, e.g.
 * `52 commits and 3 merges by 6 contributors, mostly in ui. 4 issues opened, 1 closed. 17 of 67 builds failed.`
 *
 * ## Semantics
 * - The summary has up to three sentences, one per data source, in the order version control, issue
 *   tracking, continuous integration. A source without events in the segment contributes no sentence.
 * - A segment without any events is summarised as [NO_ACTIVITY].
 * - Contributors are the distinct actors of the segment's commits and merges.
 * - The top module is named as it is, except for the root module, which is written out as
 *   `the repository root`.
 * - Nouns agree in number with their count (`1 commit`, `2 commits`).
 *
 * ## Trade-offs
 * The templates only state counts and never judge a period ("unstable", "productive"). A summary can
 * therefore always be verified against the events of its segment, at the cost of being less vivid than
 * free text.
 *
 * ## Usage
 * ```kotlin
 * val summary = NarrativeSummarizer().summarize(segment)
 * val described = segment.copy(summary = summary)
 * ```
 *
 * @see NarrativeService
 */
internal class NarrativeSummarizer {
    companion object {
        /** Summary of a segment without events. */
        const val NO_ACTIVITY = "No activity."

        /** Wording for [NarrativeService.ROOT_MODULE], whose technical name does not read well in a sentence. */
        private const val ROOT_MODULE_TEXT = "the repository root"
    }

    /**
     * Summarises the events of a segment.
     *
     * @param segment The segment to describe; its own `summary` is ignored.
     * @return The sentences of the sources that have events, separated by a blank; [NO_ACTIVITY] if the
     *   segment has no events.
     */
    fun summarize(segment: NarrativeSegment): String =
        listOfNotNull(versionControlSentence(segment), issueSentence(segment), buildSentence(segment))
            .joinToString(" ")
            .ifEmpty { NO_ACTIVITY }

    /**
     * Describes commits and merges, who made them and where.
     *
     * @param segment The segment to describe.
     * @return A sentence like `52 commits and 3 merges by 6 contributors, mostly in ui.`; `null` if the
     *   segment has neither commits nor merges.
     */
    private fun versionControlSentence(segment: NarrativeSegment): String? {
        val counts =
            listOfNotNull(
                counted(segment.count(NarrativeEventType.COMMIT), "commit"),
                counted(segment.count(NarrativeEventType.MERGE), "merge"),
            )
        if (counts.isEmpty()) return null
        val contributors =
            segment.events
                .filter { it.type.source == NarrativeSource.VCS }
                .mapNotNull { it.actor }
                .distinct()
                .size
        return buildString {
            append(counts.joinToString(" and "))
            counted(contributors, "contributor")?.let { append(" by $it") }
            segment.topModule?.let { append(", mostly in ${if (it == NarrativeService.ROOT_MODULE) ROOT_MODULE_TEXT else it}") }
            append('.')
        }
    }

    /**
     * Describes opened and closed issues.
     *
     * @param segment The segment to describe.
     * @return A sentence like `4 issues opened, 1 closed.`, `4 issues opened.` or `1 issue closed.`;
     *   `null` if no issue was opened or closed.
     */
    private fun issueSentence(segment: NarrativeSegment): String? {
        val opened = segment.count(NarrativeEventType.ISSUE_OPENED)
        val closed = segment.count(NarrativeEventType.ISSUE_CLOSED)
        return when {
            opened > 0 && closed > 0 -> "${counted(opened, "issue")} opened, $closed closed."
            opened > 0 -> "${counted(opened, "issue")} opened."
            closed > 0 -> "${counted(closed, "issue")} closed."
            else -> null
        }
    }

    /**
     * Describes the build results.
     *
     * @param segment The segment to describe.
     * @return `All 12 builds succeeded.`, `All 3 builds failed.` or `3 of 12 builds failed.`, with
     *   `1 build succeeded.` and `1 build failed.` for a single build; `null` if there was no build.
     */
    private fun buildSentence(segment: NarrativeSegment): String? {
        val failed = segment.count(NarrativeEventType.BUILD_FAILED)
        val total = failed + segment.count(NarrativeEventType.BUILD_SUCCEEDED)
        val outcome = if (failed == 0) "succeeded" else "failed"
        return when {
            total == 0 -> null
            total == 1 -> "1 build $outcome."
            failed == 0 || failed == total -> "All $total builds $outcome."
            else -> "$failed of $total builds failed."
        }
    }

    /**
     * Formats a count with its noun in the matching number.
     *
     * @param count The number of things.
     * @param noun The singular noun; its plural is formed by appending `s`.
     * @return `1 commit`, `2 commits`, ...; `null` for a count of zero so that the part can be left out.
     */
    private fun counted(
        count: Int,
        noun: String,
    ): String? =
        when (count) {
            0 -> null
            1 -> "1 $noun"
            else -> "$count ${noun}s"
        }
}
