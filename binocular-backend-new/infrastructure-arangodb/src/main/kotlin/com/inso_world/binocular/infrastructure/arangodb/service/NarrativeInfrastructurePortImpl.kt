package com.inso_world.binocular.infrastructure.arangodb.service

import com.arangodb.springframework.core.ArangoOperations
import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.service.narrative.BuildActivity
import com.inso_world.binocular.core.service.narrative.CommitActivity
import com.inso_world.binocular.core.service.narrative.IssueActivity
import com.inso_world.binocular.core.service.narrative.NarrativeInfrastructurePort
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * ArangoDB implementation of [NarrativeInfrastructurePort].
 *
 * ## Semantics
 * - Every port method is answered with exactly one AQL query. Related data (parents, files, issues,
 *   commits of a build) is collected in subqueries over the edge collections, which are served by the
 *   edge indexes on `_from` and `_to`.
 * - Timestamps are compared as epoch milliseconds via `DATE_TIMESTAMP`, because the stored date strings
 *   come in several ISO-8601 variants (with and without `Z`, with and without seconds).
 * - Documents without a usable timestamp are skipped, as they cannot be placed in a narrative.
 *
 * ## Trade-offs
 * - Results are read as plain maps and converted by hand instead of going through the entity mappers.
 *   This keeps the queries independent of the mapping sessions and avoids loading whole aggregates, at
 *   the cost of spelling out the attribute names in this class.
 * - The period filter is not served by an index. With a few thousand commits a collection scan is faster
 *   than the round trips it replaces; a persistent index on the date attributes is the next step if the
 *   mined histories grow much larger.
 *
 * @property arangoOperations Spring Data entry point used to run the AQL queries.
 */
@Service
internal class NarrativeInfrastructurePortImpl(
    private val arangoOperations: ArangoOperations,
) : NarrativeInfrastructurePort {
    companion object {
        private val logger by logger()

        private const val PERIOD_FILTER = """
              FILTER ts != null
              FILTER @since == null OR ts >= @since
              FILTER @until == null OR ts < @until"""

        private const val COMMITS_QUERY = """
            FOR c IN commits
              LET ts = DATE_TIMESTAMP(c.commitDateTime != null ? c.commitDateTime : c.date)$PERIOD_FILTER
              SORT ts ASC, c.sha ASC
              LET author = DOCUMENT(c.author)
              RETURN {
                sha: c.sha,
                timestamp: ts,
                message: c.message,
                author: author == null ? null : TRIM(SPLIT(author.gitSignature, "<")[0]),
                parentCount: LENGTH(FOR e IN `commits-commits` FILTER e._from == c._id RETURN 1),
                filePaths: (
                  FOR e IN `commits-files` FILTER e._from == c._id
                    LET f = DOCUMENT(e._to) FILTER f != null RETURN f.path
                ),
                issueNumbers: (
                  FOR e IN `issues-commits` FILTER e._to == c._id
                    LET i = DOCUMENT(e._from) FILTER i != null AND i.iid != null RETURN i.iid
                ),
                webUrl: c.webUrl
              }"""

        private const val ISSUES_QUERY = """
            FOR i IN issues
              LET created = DATE_TIMESTAMP(i.createdAt)
              LET closed = i.closedAt == null ? null : DATE_TIMESTAMP(i.closedAt)
              FILTER created != null AND i.iid != null
              LET createdInPeriod = (@since == null OR created >= @since) AND (@until == null OR created < @until)
              LET closedInPeriod = closed != null
                AND (@since == null OR closed >= @since) AND (@until == null OR closed < @until)
              FILTER createdInPeriod OR closedInPeriod
              SORT created ASC, i.iid ASC
              RETURN {
                number: i.iid,
                title: i.title,
                createdAt: created,
                closedAt: closed,
                author: FIRST(
                  FOR e IN `issues-accounts` FILTER e._from == i._id AND e.role == "author"
                    LET a = DOCUMENT(e._to) FILTER a != null RETURN a.name != null ? a.name : a.login
                ),
                webUrl: i.webUrl
              }"""

        private const val BUILDS_QUERY = """
            FOR b IN builds
              LET ts = DATE_TIMESTAMP(b.createdAt)$PERIOD_FILTER
              SORT ts ASC, b._key ASC
              RETURN {
                id: b._key,
                status: b.status,
                timestamp: ts,
                title: b.tag,
                actor: b.userFullName != null ? b.userFullName : b.user,
                commitShas: (
                  FOR e IN `commits-builds` FILTER e._to == b._id
                    LET c = DOCUMENT(e._from) FILTER c != null RETURN c.sha
                ),
                webUrl: b.webUrl
              }"""
    }

    /**
     * Finds the commits of the period with one query.
     *
     * The author name is the part of the developer's git signature before the e-mail address. A commit
     * is linked to its parents through `commits-commits` edges pointing from child to parent.
     */
    override fun findCommitActivities(
        since: LocalDateTime?,
        until: LocalDateTime?,
    ): List<CommitActivity> =
        query(COMMITS_QUERY, since, until).map { row ->
            CommitActivity(
                sha = row["sha"] as String,
                timestamp = toLocalDateTime(row["timestamp"]),
                message = row["message"] as? String ?: "",
                author = row["author"] as? String,
                parentCount = (row["parentCount"] as Number).toInt(),
                filePaths = strings(row["filePaths"]),
                issueNumbers = (row["issueNumbers"] as? List<*>).orEmpty().filterIsInstance<Number>().map { it.toInt() },
                webUrl = row["webUrl"] as? String,
            )
        }

    /**
     * Finds the issues opened or closed in the period with one query.
     *
     * The author is the account linked with the role `author`; its name is used and, if the account has
     * no name, its login. Issues without a platform number are skipped.
     */
    override fun findIssueActivities(
        since: LocalDateTime?,
        until: LocalDateTime?,
    ): List<IssueActivity> =
        query(ISSUES_QUERY, since, until).map { row ->
            IssueActivity(
                number = (row["number"] as Number).toInt(),
                title = row["title"] as? String ?: "",
                createdAt = toLocalDateTime(row["createdAt"]),
                closedAt = row["closedAt"]?.let { toLocalDateTime(it) },
                author = row["author"] as? String,
                webUrl = row["webUrl"] as? String,
            )
        }

    /**
     * Finds the builds created in the period with one query.
     *
     * The build is identified by its document key, the actor is the full name of the triggering user
     * and, if that is missing, the user's login.
     */
    override fun findBuildActivities(
        since: LocalDateTime?,
        until: LocalDateTime?,
    ): List<BuildActivity> =
        query(BUILDS_QUERY, since, until).map { row ->
            BuildActivity(
                id = row["id"] as String,
                status = row["status"] as? String,
                timestamp = toLocalDateTime(row["timestamp"]),
                title = row["title"] as? String,
                actor = row["actor"] as? String,
                commitShas = strings(row["commitShas"]),
                webUrl = row["webUrl"] as? String,
            )
        }

    /**
     * Runs a period query and returns its rows.
     *
     * @param aql The query; it must use the bind variables `@since` and `@until`.
     * @param since Start of the period, bound as epoch milliseconds or `null`.
     * @param until End of the period, bound as epoch milliseconds or `null`.
     * @return One map per result row, keyed by the attribute names of the query's `RETURN` object.
     */
    private fun query(
        aql: String,
        since: LocalDateTime?,
        until: LocalDateTime?,
    ): List<Map<*, *>> {
        logger.trace("Running narrative query for period [{}, {})", since, until)
        val bindVars = mapOf("since" to since?.toEpochMilli(), "until" to until?.toEpochMilli())
        return arangoOperations.query(aql, bindVars, Map::class.java).asListRemaining()
    }

    /** Converts a UTC date-time to epoch milliseconds, the unit the queries compare in. */
    private fun LocalDateTime.toEpochMilli(): Long = toInstant(ZoneOffset.UTC).toEpochMilli()

    /** Converts epoch milliseconds returned by a query back to a UTC date-time. */
    private fun toLocalDateTime(epochMilli: Any?): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli((epochMilli as Number).toLong()), ZoneOffset.UTC)

    /** Reads a query result array as a list of strings; a missing array is an empty list. */
    private fun strings(value: Any?): List<String> = (value as? List<*>).orEmpty().filterIsInstance<String>()
}
