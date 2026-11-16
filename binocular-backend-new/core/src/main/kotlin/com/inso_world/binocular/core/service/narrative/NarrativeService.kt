package com.inso_world.binocular.core.service.narrative

import com.inso_world.binocular.model.narrative.NarrativeEntityRef
import com.inso_world.binocular.model.narrative.NarrativeEvent
import com.inso_world.binocular.model.narrative.NarrativeEventType
import com.inso_world.binocular.model.narrative.NarrativeGranularity
import com.inso_world.binocular.model.narrative.NarrativeSegment
import com.inso_world.binocular.model.narrative.NarrativeSource
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

/**
 * Composes the raw activity of a repository into a software history narrative.
 *
 * ## Purpose
 * Turns the commits, issues and builds delivered by [NarrativeInfrastructurePort] into [NarrativeEvent]s,
 * correlates them across the three sources and cuts the result into chronologically ordered
 * [NarrativeSegment]s. All interpretation of the mined data happens here, not in the adapters.
 *
 * ## Semantics
 * - **Events.** A commit with two or more parents becomes a merge, every other commit a commit event.
 *   An issue becomes an opened event and, if closed, a closed event. A build becomes a succeeded or
 *   failed event; builds with any other status (e.g. cancelled) say nothing about the code and are left out.
 * - **Entity linking.** The stored links are used in both directions: a commit names its issues and
 *   builds, an issue its commits, a build its commits.
 * - **Temporal proximity.** A build without a stored link is attributed to the latest commit made at most
 *   [BUILD_PROXIMITY] before it. This is the only correlation that is inferred rather than stored.
 * - **Modules.** The module of a file is its top-level directory, [ROOT_MODULE] for files in the
 *   repository root. Issues and builds inherit the modules of their commits.
 * - **Segments.** Segments are consecutive and gap-free from the period of the first event to the period
 *   of the last one; a period without events yields an empty segment.
 *
 * ## Trade-offs
 * - The class carries no Spring annotation on purpose. The port is only implemented by the ArangoDB
 *   adapter, so an application module that needs the service declares it as a bean itself; a
 *   component-scanned service would break every application context without that adapter.
 * - Only activity inside the requested period is loaded. A link to an entity outside the period is kept
 *   as a reference, but that entity contributes neither events nor modules.
 *
 * ## Usage
 * ```kotlin
 * val service = NarrativeService(narrativePort)
 * val months = service.compose(NarrativeGranularity.MONTH)
 * val weeksOfMarch = service.compose(
 *     granularity = NarrativeGranularity.WEEK,
 *     since = LocalDateTime.of(2025, 3, 1, 0, 0),
 *     until = LocalDateTime.of(2025, 4, 1, 0, 0),
 * )
 * ```
 *
 * @property narrativePort Source of the raw commit, issue and build activity.
 * @see NarrativeInfrastructurePort
 * @see NarrativeSegment
 */
class NarrativeService(
    private val narrativePort: NarrativeInfrastructurePort,
) {
    companion object {
        /**
         * Longest time between a commit and a build for the build to be attributed to that commit when no
         * link is stored.
         *
         * Ten minutes keep the inferred links reliable: on the Binocular reference data set, 98 % of the
         * builds that this rule attributes to a commit (1269 of 1290) are attributed to the commit they are
         * actually linked to. Longer windows link more builds but lose precision.
         */
        val BUILD_PROXIMITY: Duration = Duration.ofMinutes(10)

        /** Module name of files that lie directly in the repository root. */
        const val ROOT_MODULE = "(root)"

        private val SUCCEEDED_STATUSES = setOf("success")
        private val FAILED_STATUSES = setOf("failed", "failure", "startup_failure")
    }

    /**
     * Composes the narrative of the given period.
     *
     * @param granularity The time unit the narrative is cut into.
     * @param since Start of the period, inclusive; `null` for no lower bound.
     * @param until End of the period, exclusive; `null` for no upper bound.
     * @return The segments in chronological order; empty if the period contains no events.
     */
    fun compose(
        granularity: NarrativeGranularity,
        since: LocalDateTime? = null,
        until: LocalDateTime? = null,
    ): List<NarrativeSegment> {
        val commits = narrativePort.findCommitActivities(since, until)
        val issues = narrativePort.findIssueActivities(since, until)
        val builds = narrativePort.findBuildActivities(since, until).filter { eventTypeOf(it) != null }

        val modulesBySha = commits.associate { it.sha to modulesOf(it) }
        val shasByBuild = builds.associate { it.id to commitShasOf(it, commits) }
        val buildsBySha = invert(shasByBuild)
        val shasByIssue = invert(commits.associate { it.sha to it.issueNumbers })
        val modulesOfShas = { shas: List<String> -> shas.flatMap { modulesBySha[it].orEmpty() }.distinct().sorted() }
        val inPeriod = { timestamp: LocalDateTime ->
            (since == null || !timestamp.isBefore(since)) && (until == null || timestamp.isBefore(until))
        }

        val events =
            commits.map { commitEvent(it, modulesBySha.getValue(it.sha), buildsBySha[it.sha].orEmpty()) } +
                issues.flatMap { issue ->
                    val shas = shasByIssue[issue.number].orEmpty()
                    issueEvents(issue, shas, modulesOfShas(shas)).filter { inPeriod(it.timestamp) }
                } +
                builds.map { build ->
                    val shas = shasByBuild.getValue(build.id)
                    buildEvent(build, shas, modulesOfShas(shas))
                }

        return segmentsOf(
            granularity,
            events
                .distinctBy { it.id }
                .sortedWith(compareBy<NarrativeEvent> { it.timestamp }.thenBy { it.type }.thenBy { it.entity.ref }),
        )
    }

    /**
     * Maps a build to the type of event it produces.
     *
     * @param build The build to classify by its platform status.
     * @return [NarrativeEventType.BUILD_SUCCEEDED] or [NarrativeEventType.BUILD_FAILED]; `null` if the
     *   status describes neither a success nor a failure, in which case the build produces no event.
     */
    private fun eventTypeOf(build: BuildActivity): NarrativeEventType? =
        when (build.status?.lowercase()) {
            in SUCCEEDED_STATUSES -> NarrativeEventType.BUILD_SUCCEEDED
            in FAILED_STATUSES -> NarrativeEventType.BUILD_FAILED
            else -> null
        }

    /**
     * Derives the modules a commit touched from its changed files.
     *
     * @param commit The commit whose file paths are examined.
     * @return The distinct top-level directories in alphabetical order, with [ROOT_MODULE] standing for
     *   files in the repository root.
     */
    private fun modulesOf(commit: CommitActivity): List<String> =
        commit.filePaths
            .map { path -> path.substringBefore('/', missingDelimiterValue = ROOT_MODULE) }
            .distinct()
            .sorted()

    /**
     * Determines the commits a build belongs to.
     *
     * Stored links always win. Without one, the build is attributed to the latest commit that was made
     * at or before the build and no longer than [BUILD_PROXIMITY] ago.
     *
     * @param build The build to attribute.
     * @param commits The commits of the period in chronological order.
     * @return The SHAs of the build's commits; empty if there is neither a stored link nor a commit
     *   close enough in time.
     */
    private fun commitShasOf(
        build: BuildActivity,
        commits: List<CommitActivity>,
    ): List<String> {
        if (build.commitShas.isNotEmpty()) return build.commitShas.distinct()
        val latest = commits.lastOrNull { !it.timestamp.isAfter(build.timestamp) } ?: return emptyList()
        return if (Duration.between(latest.timestamp, build.timestamp) <= BUILD_PROXIMITY) listOf(latest.sha) else emptyList()
    }

    /**
     * Turns a relation around, e.g. commits per build into builds per commit.
     *
     * @param valuesByKey For every key the values it is related to.
     * @return For every value the keys that are related to it, in the iteration order of [valuesByKey].
     */
    private fun <K, V> invert(valuesByKey: Map<K, List<V>>): Map<V, List<K>> =
        valuesByKey
            .flatMap { (key, values) -> values.distinct().map { it to key } }
            .groupBy({ it.first }, { it.second })

    /**
     * Creates the event of a commit.
     *
     * @param commit The commit; two or more parents make it a merge.
     * @param modules The modules the commit touched.
     * @param buildIds The ids of the builds that belong to the commit.
     * @return A commit or merge event titled with the first line of the commit message and related to
     *   the commit's issues and builds.
     */
    private fun commitEvent(
        commit: CommitActivity,
        modules: List<String>,
        buildIds: List<String>,
    ) = NarrativeEvent(
        type = if (commit.parentCount >= 2) NarrativeEventType.MERGE else NarrativeEventType.COMMIT,
        entity = NarrativeEntityRef(NarrativeSource.VCS, commit.sha),
        timestamp = commit.timestamp,
        title =
            commit.message
                .lineSequence()
                .firstOrNull()
                .orEmpty()
                .trim(),
        actor = commit.author,
        modules = modules,
        relatedEntities =
            commit.issueNumbers.distinct().map { NarrativeEntityRef(NarrativeSource.ITS, it.toString()) } +
                buildIds.map { NarrativeEntityRef(NarrativeSource.CI, it) },
        webUrl = commit.webUrl,
    )

    /**
     * Creates the events of an issue.
     *
     * The closed event has no actor because the mined data does not record who closed an issue.
     *
     * @param issue The issue.
     * @param commitShas The SHAs of the commits that refer to the issue.
     * @param modules The modules of those commits.
     * @return The opened event and, if the issue is closed, the closed event.
     */
    private fun issueEvents(
        issue: IssueActivity,
        commitShas: List<String>,
        modules: List<String>,
    ): List<NarrativeEvent> {
        val opened =
            NarrativeEvent(
                type = NarrativeEventType.ISSUE_OPENED,
                entity = NarrativeEntityRef(NarrativeSource.ITS, issue.number.toString()),
                timestamp = issue.createdAt,
                title = issue.title,
                actor = issue.author,
                modules = modules,
                relatedEntities = commitShas.map { NarrativeEntityRef(NarrativeSource.VCS, it) },
                webUrl = issue.webUrl,
            )
        val closed = issue.closedAt?.let { opened.copy(type = NarrativeEventType.ISSUE_CLOSED, timestamp = it, actor = null) }
        return listOfNotNull(opened, closed)
    }

    /**
     * Creates the event of a build.
     *
     * @param build A build whose status maps to an event type.
     * @param commitShas The SHAs of the commits the build belongs to.
     * @param modules The modules of those commits.
     * @return A succeeded or failed event titled with the build's title or, if it has none, its id.
     */
    private fun buildEvent(
        build: BuildActivity,
        commitShas: List<String>,
        modules: List<String>,
    ) = NarrativeEvent(
        type = checkNotNull(eventTypeOf(build)) { "build ${build.id} has no narrative event type" },
        entity = NarrativeEntityRef(NarrativeSource.CI, build.id),
        timestamp = build.timestamp,
        title = build.title?.takeIf { it.isNotBlank() } ?: "Build ${build.id}",
        actor = build.actor,
        modules = modules,
        relatedEntities = commitShas.map { NarrativeEntityRef(NarrativeSource.VCS, it) },
        webUrl = build.webUrl,
    )

    /**
     * Cuts chronologically ordered events into consecutive segments.
     *
     * @param granularity The time unit of a segment.
     * @param events All events of the narrative in chronological order.
     * @return One segment per period from the first to the last event, including empty ones in between.
     */
    private fun segmentsOf(
        granularity: NarrativeGranularity,
        events: List<NarrativeEvent>,
    ): List<NarrativeSegment> {
        if (events.isEmpty()) return emptyList()
        val eventsByStart = events.groupBy { startOf(granularity, it.timestamp) }
        val lastStart = startOf(granularity, events.last().timestamp)
        return generateSequence(startOf(granularity, events.first().timestamp)) { endOf(granularity, it) }
            .takeWhile { !it.isAfter(lastStart) }
            .map { start ->
                val segmentEvents = eventsByStart[start].orEmpty()
                NarrativeSegment(
                    granularity = granularity,
                    start = start,
                    end = endOf(granularity, start),
                    events = segmentEvents,
                    topModule = topModuleOf(segmentEvents),
                )
            }.toList()
    }

    /**
     * Finds the start of the period a timestamp falls into.
     *
     * @param granularity The time unit of a period.
     * @param timestamp Any moment within the period.
     * @return Midnight of the first day of the month, or of the Monday of the ISO week.
     */
    private fun startOf(
        granularity: NarrativeGranularity,
        timestamp: LocalDateTime,
    ): LocalDateTime {
        val date = timestamp.toLocalDate()
        return when (granularity) {
            NarrativeGranularity.MONTH -> date.withDayOfMonth(1)
            NarrativeGranularity.WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        }.atStartOfDay()
    }

    /**
     * Finds the exclusive end of the period that starts at [start], which is the start of the next period.
     *
     * @param granularity The time unit of a period.
     * @param start The start of a period as returned by [startOf].
     * @return [start] plus one month or one week.
     */
    private fun endOf(
        granularity: NarrativeGranularity,
        start: LocalDateTime,
    ): LocalDateTime =
        when (granularity) {
            NarrativeGranularity.MONTH -> start.plusMonths(1)
            NarrativeGranularity.WEEK -> start.plusWeeks(1)
        }

    /**
     * Finds the module most commits and merges of a segment touched.
     *
     * @param events The events of one segment.
     * @return The module touched by the most commit and merge events, the alphabetically first one on a
     *   tie; `null` if no such event names a module.
     */
    private fun topModuleOf(events: List<NarrativeEvent>): String? =
        events
            .filter { it.type.source == NarrativeSource.VCS }
            .flatMap { it.modules }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .firstOrNull()
            ?.key
}
