package com.inso_world.binocular.core.service.usecase

import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.model.Build
import com.inso_world.binocular.model.metrics.BusFactorCIErrorRate
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.enums.Granularity
import com.inso_world.binocular.model.metrics.AuthorContribution
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

@Service
open class GetBusFactorCIErrorRateMetric(
    @Autowired private val repoPort: RepositoryInfrastructurePort,
) {

    @MappingSession
    open fun execute(
        repoPath: String, since: Long, until: Long, granularity: Granularity
    ): List<BusFactorCIErrorRate> {
        //repo not used
        val repo = repoPort.findByName(repoPath)

        val start = Instant.ofEpochMilli(since).atZone(ZoneOffset.UTC).toLocalDate()
        val end = Instant.ofEpochMilli(until).atZone(ZoneOffset.UTC).toLocalDate()

        val builds = repoPort.findAllBuilds(repo).toList()
        val commits = repoPort.findAllCommits(repo).toList()

        val periods = buildPeriods(start, end, granularity)

        return periods.mapIndexed { index, period ->
            val commitsUpToPeriod = commits.asSequence().filter { c ->
                !c.authorDateTime.toLocalDate().isAfter(period.end)
            }
            val periodBuilds = builds.asSequence().filter { b ->
                val d = b.createdAt?.toLocalDate() ?: return@filter false
                !d.isBefore(period.start) && !d.isAfter(period.end)
            }

            val bf = calculateBusFactor(commitsUpToPeriod)

            BusFactorCIErrorRate(
                id = period.label,
                busFactor = bf.busFactor,
                ciErrorRate = calculateCiErrorRate(periodBuilds),
                topAuthors = bf.topAuthors,
            )
        }
    }

    private data class Period(val start: LocalDate, val end: LocalDate, val label: String)

    private data class BusFactorResult(val busFactor: Int, val topAuthors: List<AuthorContribution>)

    private fun calculateCiErrorRate(builds: Sequence<Build>): Double {
        var failed = 0
        var completed = 0
        for (build in builds) {
            when (build.status?.lowercase()) {
                "failed" -> {
                    failed++; completed++
                }

                "success" -> completed++
            }
        }
        return if (completed == 0) 0.0 else failed.toDouble() / completed
    }

    private fun calculateBusFactor(commits: Sequence<Commit>): BusFactorResult {
        val commitsPerAuthor: Map<Developer, Int> = commits
            .groupingBy { it.author }
            .eachCount()

        val totalCommits = commitsPerAuthor.values.sum()
        if (totalCommits == 0) return BusFactorResult(0, emptyList())

        val sorted = commitsPerAuthor.entries.sortedByDescending { it.value }

        val threshold = totalCommits / 2.0
        var accumulated = 0
        val topAuthors = mutableListOf<AuthorContribution>()
        for ((author, count) in sorted) {
            accumulated += count
            topAuthors += AuthorContribution(
                gitSignature = author.gitSignature,
                percentage = count.toDouble() / totalCommits,
            )
            if (accumulated > threshold) break
        }
        return BusFactorResult(topAuthors.size, topAuthors)
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
                    periods += Period(cursor.atDay(1), cursor.atEndOfMonth(), "%02d/%d".format(cursor.monthValue, cursor.year))
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
}
