package com.inso_world.binocular.core.service.narrative

import java.time.LocalDateTime

/**
 * Port for reading the raw activity a software history narrative is composed from.
 *
 * ## Purpose
 * Gives the narrative service everything it needs from version control, issue tracking and continuous
 * integration for a period of time: the commits, issues and builds themselves and the links stored
 * between them.
 *
 * ## Semantics
 * - A period is the half-open interval from `since` (inclusive) to `until` (exclusive). A `null` bound
 *   leaves that side of the period open, so two `null` bounds select the whole history.
 * - Every method returns its records in chronological order.
 * - Adapters return the stored facts unchanged. They do not decide what a merge is, how a build status
 *   is to be read or which activities belong together beyond the stored links; that is domain logic of
 *   the narrative service.
 *
 * ## Trade-offs
 * - This port does not extend `BinocularInfrastructurePort`: it is a read-only query across several
 *   aggregates, not CRUD access to one of them.
 * - It returns lightweight read models instead of domain aggregates. A narrative spans thousands of
 *   commits, and loading each of them as an aggregate with its relations costs one query per commit and
 *   relation. Adapters are expected to answer each method with a small, fixed number of queries.
 *
 * ## Usage
 * ```kotlin
 * val march = LocalDateTime.of(2025, 3, 1, 0, 0)
 * val april = LocalDateTime.of(2025, 4, 1, 0, 0)
 * val commits = port.findCommitActivities(since = march, until = april)
 * val wholeHistory = port.findCommitActivities(since = null, until = null)
 * ```
 *
 * @see CommitActivity
 * @see IssueActivity
 * @see BuildActivity
 */
interface NarrativeInfrastructurePort {
    /**
     * Finds the commits made within the given period.
     *
     * @param since Start of the period, inclusive; `null` for no lower bound.
     * @param until End of the period, exclusive; `null` for no upper bound.
     * @return The commits whose timestamp lies in the period, ordered by timestamp ascending.
     */
    fun findCommitActivities(
        since: LocalDateTime?,
        until: LocalDateTime?,
    ): List<CommitActivity>

    /**
     * Finds the issues that were opened or closed within the given period.
     *
     * An issue is returned if its creation or its closing lies in the period, because either of them
     * is a narrative event of that period.
     *
     * @param since Start of the period, inclusive; `null` for no lower bound.
     * @param until End of the period, exclusive; `null` for no upper bound.
     * @return The matching issues, ordered by creation time ascending.
     */
    fun findIssueActivities(
        since: LocalDateTime?,
        until: LocalDateTime?,
    ): List<IssueActivity>

    /**
     * Finds the CI builds created within the given period.
     *
     * @param since Start of the period, inclusive; `null` for no lower bound.
     * @param until End of the period, exclusive; `null` for no upper bound.
     * @return The builds whose timestamp lies in the period, ordered by timestamp ascending.
     */
    fun findBuildActivities(
        since: LocalDateTime?,
        until: LocalDateTime?,
    ): List<BuildActivity>
}
