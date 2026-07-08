package com.inso_world.binocular.web.usecases

import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.model.enums.Granularity
import com.inso_world.binocular.model.metrics.AuthorContribution
import com.inso_world.binocular.model.metrics.BusFactorCIErrorRate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import kotlin.collections.plusAssign

/**
 * Use case that computes the "Bus Factor vs. CI Error Rate" quadrant metric.
 *
 * There are two entry points:
 *  - [execute]  -> results over TIME (one data point per month/year in the range).
 *  - [execute2] -> results per MODULE (one data point per module path).
 *
 * Bus factor = the minimum number of top authors whose commits together make up
 * MORE than 50% of all commits. A low bus factor (1) means a lot of the knowledge
 * sits with very few people, so losing them would hurt.
 *
 * CI error rate = failed builds / completed builds (completed = failed + success).
 *
 * All the heavy grouping/counting happens in the database (AQL) through [repoPort].
 * This class only does the small, cheap math on top of those aggregated numbers.
 */
@Service
class GetBusFactorCIErrorRateMetric(
    @Autowired private val repoPort: RepositoryInfrastructurePort,
) {

    /**
     * Timeline variant: computes bus factor + CI error rate for each period
     * (month or year) between [since] and [until].
     *
     * @param repoPath    name of the repository to look at
     * @param since       start of the range as epoch millis (UTC)
     * @param until       end of the range as epoch millis (UTC)
     * @param granularity whether we bucket the timeline by MONTH or by YEAR
     */
    @MappingSession
    fun execute(repoPath: String, since: Long, until: Long, granularity: Granularity): List<BusFactorCIErrorRate> {

        val repo = repoPort.findByName(repoPath)

        // Convert the incoming epoch millis into calendar dates (UTC) so we can build periods from them.
        val start = Instant.ofEpochMilli(since).atZone(ZoneOffset.UTC).toLocalDate()
        val end = Instant.ofEpochMilli(until).atZone(ZoneOffset.UTC).toLocalDate()

        // Build the list of periods (e.g. one entry per month) that we want data points for.
        val periods = buildPeriods(start, end, granularity)
        if (periods.isEmpty()) return emptyList()

        // Date format string the database uses to label each bucket.
        // It MUST produce the same labels as buildPeriods() below, otherwise the lookups by label won't match.
        val fmt = when (granularity) {
            Granularity.MONTH -> "%mm/%yyyy"
            Granularity.YEAR -> "%yyyy"
        }

        // We query the DB with period-aligned bounds (start of the first period ... end of the last period),
        // not the raw since/until, so a partial first/last month is handled the same way as buildPeriods().
        val firstStartMillis = periods.first().start.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val lastEndMillis = periods.last().end
            .plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() - 1 // last millisecond of the last day

        val firstLabel = periods.first().label

        // CI error rate per period, straight from the DB aggregation.
        // We turn each bucket (failed, completed) into a ready-to-use rate, guarding against divide-by-zero.
        val ciByPeriod = repoPort.findCiErrorRateBuckets(repo, firstStartMillis, lastEndMillis, fmt)
            .associate { it.period to (if (it.completed == 0L) 0.0 else it.failed.toDouble() / it.completed) }

        // Commit counts grouped by (author, period). The bus factor is CUMULATIVE over time,
        // so we fetch per-period counts and add them up as we walk through the periods below.
        val countsByPeriod = repoPort
            .findAuthorCommitCountsByPeriod(repo, lastEndMillis, firstStartMillis, firstLabel, fmt)
            .groupBy { it.period }

        // "running" keeps the cumulative commit count per author up to the current period.
        val running = HashMap<String, Int>()
        return periods.map { period ->
            // Add this period's commits on top of the running totals (cumulative sum).
            countsByPeriod[period.label]?.forEach { running.merge(it.gitSignature, it.count.toInt(), Int::plus) }

            // Bus factor is computed on the cumulative totals as they stand at this period.
            val bf = calculateBusFactor(running)
            BusFactorCIErrorRate(
                module = period.label, // for the timeline variant this field holds the period label, not a module
                busFactor = bf.busFactor,
                ciErrorRate = ciByPeriod[period.label] ?: 0.0, // periods without builds default to 0.0
                topAuthors = bf.topAuthors,
            )
        }
    }

    /** One time bucket: its date range plus the label shown to the user (e.g. "06/2026"). */
    private data class Period(val start: LocalDate, val end: LocalDate, val label: String)

    /** Result of a bus factor calculation: the number itself plus the authors that make up the majority. */
    private data class BusFactorResult(val busFactor: Int, val topAuthors: List<AuthorContribution>)

    /**
     * Calculates the bus factor for the timeline variant.
     *
     * Idea: sort authors by how many commits they have (most first), then keep adding
     * them up until we pass the 50% mark. The number of authors we needed is the bus factor.
     *
     * @param commitsPerAuthor map of gitSignature -> commit count
     */
    private fun calculateBusFactor(commitsPerAuthor: Map<String, Int>): BusFactorResult {
        val total = commitsPerAuthor.values.sum()
        if (total == 0) return BusFactorResult(0, emptyList()) // no commits -> nothing to measure

        // Biggest contributors first.
        val sorted = commitsPerAuthor.entries.sortedByDescending { it.value }

        val threshold = total / 2.0 // the "more than 50%" line we want to cross
        var accumulated = 0
        val top = mutableListOf<AuthorContribution>()
        for ((gitSignature, count) in sorted) {
            accumulated += count
            // Remember this author and their share of the total.
            top += AuthorContribution(gitSignature = gitSignature, percentage = count.toDouble() / total)
            // As soon as the authors collected so far own more than half, we have our bus factor.
            if (accumulated > threshold) break
        }
        return BusFactorResult(top.size, top)
    }

    /**
     * Builds the list of periods between [start] and [end].
     *
     * MONTH -> one period per calendar month (from the 1st to the last day of the month).
     * YEAR  -> one period per calendar year (Jan 1st to Dec 31st).
     *
     * The label format here MUST stay in sync with the DB `fmt` string used in execute().
     */
    private fun buildPeriods(
        start: LocalDate, end: LocalDate, granularity: Granularity
    ): List<Period> {
        val periods = mutableListOf<Period>()
        when (granularity) {
            Granularity.MONTH -> {
                // Walk month by month from the start month up to (and including) the end month.
                var cursor = YearMonth.from(start)
                val last = YearMonth.from(end)
                while (!cursor.isAfter(last)) {
                    periods += Period(
                        cursor.atDay(1),        // first day of the month
                        cursor.atEndOfMonth(),  // last day of the month
                        "%02d/%d".format(cursor.monthValue, cursor.year) // e.g. "06/2026"
                    )
                    cursor = cursor.plusMonths(1)
                }
            }

            Granularity.YEAR -> {
                // One period for each full calendar year in the range.
                for (year in start.year..end.year) {
                    periods += Period(
                        LocalDate.of(year, 1, 1),
                        LocalDate.of(year, 12, 31),
                        year.toString(),
                    )
                }
            }
        }
        return periods
    }

    /**
     * Module variant: computes bus factor + CI error rate PER MODULE.
     *
     * This is the "what if these people leave?" analysis:
     *  - [excludedAuthors] are treated as gone. Their commits still count towards the module total,
     *    but they can no longer be part of the people that "cover" the code.
     *  - [neededModules] restricts the result to specific module paths (empty list = all modules).
     *
     * Bus factor here is calculated over the WHOLE history (no time filter), while the CI error
     * rate still uses the [since]/[until] window.
     */
    @MappingSession
    fun execute2(repoPath: String, since: Long, until: Long, excludedAuthors: List<String>, neededModules: List<String>): List<BusFactorCIErrorRate> {

        val repo = repoPort.findByName(repoPath)

        // Put the excluded authors in a set for fast lookups.
        val excluded = excludedAuthors.toHashSet()

        // CI error rate per module, already aggregated in the DB and turned into a rate.
        val ciByModule = repoPort.findCiErrorRateByModule(repo, since, until, neededModules)
            .associate { it.module to (if (it.completed == 0L) 0.0 else it.failed.toDouble() / it.completed) }

        // Commit counts per (module, author). NOTE: this includes the excluded authors on purpose,
        // because we need the FULL module total to judge whether the remaining people still hold the majority.
        val countsByModule = repoPort.countCommitsByModule(repo, neededModules)
            .groupBy { it.module }

        // Look at every module that shows up in either result set.
        val modules = (countsByModule.keys + ciByModule.keys).toSortedSet()
        return modules.map { module ->
            // author -> commit count for this module (all authors).
            val allCounts = countsByModule[module].orEmpty().associate { it.gitSignature to it.count.toInt() }
            val bf = busFactorForModule(allCounts, excluded)
            BusFactorCIErrorRate(
                module = module,
                busFactor = bf.busFactor,
                ciErrorRate = ciByModule[module] ?: 0.0,
                topAuthors = bf.topAuthors,
            )
        }
    }

    /**
     * Bus factor for a single module, taking excluded authors into account.
     *
     * Important detail: the 50% threshold is measured against [totalAll], which INCLUDES the
     * excluded authors' commits. This is what makes the "departure" case possible:
     * if the people who are left cannot even reach 50% of the module's total, we return a
     * bus factor of 0 and list everyone who remains.
     *
     * @param allCounts gitSignature -> commit count for ALL authors of the module
     * @param excluded  gitSignatures of authors we pretend have left
     */
    private fun busFactorForModule(
        allCounts: Map<String, Int>,
        excluded: Set<String>,
    ): BusFactorResult {
        val totalAll = allCounts.values.sum() // denominator = everything, including excluded authors
        if (totalAll == 0) return BusFactorResult(0, emptyList())

        // Only the authors that are still around, biggest contributors first.
        val remaining = allCounts.entries
            .filter { it.key !in excluded }
            .sortedByDescending { it.value }

        val threshold = totalAll / 2.0
        var accumulated = 0
        val top = mutableListOf<AuthorContribution>()
        for ((gitSignature, count) in remaining) {
            accumulated += count
            // Percentage is relative to the full module total, so these can add up to <= 50% in the failure case.
            top += AuthorContribution(gitSignature = gitSignature, percentage = count.toDouble() / totalAll)
            if (accumulated > threshold) {
                // The remaining authors DO cross 50% -> normal bus factor = how many we needed.
                return BusFactorResult(top.size, top)
            }
        }
        // We went through every remaining author and never crossed 50%.
        // The module can no longer be "covered" by the people left -> bus factor 0, return all of them.
        return BusFactorResult(0, top)
    }
}
