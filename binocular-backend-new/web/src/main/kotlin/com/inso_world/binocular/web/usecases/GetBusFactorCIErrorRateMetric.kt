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

@Service
class GetBusFactorCIErrorRateMetric(
    @Autowired private val repoPort: RepositoryInfrastructurePort,
) {

    @MappingSession
    fun execute(repoPath: String, since: Long, until: Long, granularity: Granularity): List<BusFactorCIErrorRate> {

        val repo = repoPort.findByName(repoPath)
        val start = Instant.ofEpochMilli(since).atZone(ZoneOffset.UTC).toLocalDate()
        val end = Instant.ofEpochMilli(until).atZone(ZoneOffset.UTC).toLocalDate()
        val periods = buildPeriods(start, end, granularity)
        if (periods.isEmpty()) return emptyList()

        val fmt = when (granularity) {
            Granularity.MONTH -> "%mm/%yyyy"
            Granularity.YEAR -> "%yyyy"
        }
        val firstStartMillis = periods.first().start.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val lastEndMillis = periods.last().end
            .plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() - 1

        val firstLabel = periods.first().label

        val ciByPeriod = repoPort.findCiErrorRateBuckets(repo, firstStartMillis, lastEndMillis, fmt)
            .associate { it.period to (if (it.completed == 0L) 0.0 else it.failed.toDouble() / it.completed) }

        val countsByPeriod = repoPort
            .findAuthorCommitCountsByPeriod(repo, lastEndMillis, firstStartMillis, firstLabel, fmt)
            .groupBy { it.period }

        val running = HashMap<String, Int>()
        return periods.map { period ->
            countsByPeriod[period.label]?.forEach { running.merge(it.gitSignature, it.count.toInt(), Int::plus) }
            val bf = calculateBusFactor(running)
            BusFactorCIErrorRate(
                module = period.label,
                busFactor = bf.busFactor,
                ciErrorRate = ciByPeriod[period.label] ?: 0.0,
                topAuthors = bf.topAuthors,
            )
        }
    }

    private data class Period(val start: LocalDate, val end: LocalDate, val label: String)

    private data class BusFactorResult(val busFactor: Int, val topAuthors: List<AuthorContribution>)

    private fun calculateBusFactor(commitsPerAuthor: Map<String, Int>): BusFactorResult {
        val total = commitsPerAuthor.values.sum()
        if (total == 0) return BusFactorResult(0, emptyList())
        val sorted = commitsPerAuthor.entries.sortedByDescending { it.value }
        val threshold = total / 2.0
        var accumulated = 0
        val top = mutableListOf<AuthorContribution>()
        for ((gitSignature, count) in sorted) {
            accumulated += count
            top += AuthorContribution(gitSignature = gitSignature, percentage = count.toDouble() / total)
            if (accumulated > threshold) break
        }
        return BusFactorResult(top.size, top)
    }

    private fun buildPeriods(
        start: LocalDate, end: LocalDate, granularity: Granularity
    ): List<Period> {
        val periods = mutableListOf<Period>()
        when (granularity) {
            Granularity.MONTH -> {
                var cursor = YearMonth.from(start)
                val last = YearMonth.from(end)
                while (!cursor.isAfter(last)) {
                    periods += Period(
                        cursor.atDay(1),
                        cursor.atEndOfMonth(),
                        "%02d/%d".format(cursor.monthValue, cursor.year)
                    )
                    cursor = cursor.plusMonths(1)
                }
            }

            Granularity.YEAR -> {
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

    @MappingSession
    fun execute2(repoPath: String, since: Long, until: Long, excludedAuthors: List<String>, neededModules: List<String>): List<BusFactorCIErrorRate> {

        val repo = repoPort.findByName(repoPath)

        val excluded = excludedAuthors.toHashSet()

        val ciByModule = repoPort.findCiErrorRateByModule(repo, since, until, neededModules)
            .associate { it.module to (if (it.completed == 0L) 0.0 else it.failed.toDouble() / it.completed) }

        val countsByModule = repoPort.countCommitsByModule(repo,neededModules)
            .groupBy { it.module }

        val modules = (countsByModule.keys + ciByModule.keys).toSortedSet()
        return modules.map { module ->
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

    private fun busFactorForModule(
        allCounts: Map<String, Int>,
        excluded: Set<String>,
    ): BusFactorResult {
        val totalAll = allCounts.values.sum()
        if (totalAll == 0) return BusFactorResult(0, emptyList())

        val remaining = allCounts.entries
            .filter { it.key !in excluded }
            .sortedByDescending { it.value }

        val threshold = totalAll / 2.0
        var accumulated = 0
        val top = mutableListOf<AuthorContribution>()
        for ((gitSignature, count) in remaining) {
            accumulated += count
            top += AuthorContribution(gitSignature = gitSignature, percentage = count.toDouble() / totalAll)
            if (accumulated > threshold) {
                return BusFactorResult(top.size, top)
            }
        }
        return BusFactorResult(0, top)
    }
}
