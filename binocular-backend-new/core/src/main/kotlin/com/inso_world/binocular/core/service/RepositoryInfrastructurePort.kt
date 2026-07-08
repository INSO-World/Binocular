package com.inso_world.binocular.core.service

import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Build
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.metrics.AuthorCountPerModule
import com.inso_world.binocular.model.metrics.AuthorPeriodCount
import com.inso_world.binocular.model.metrics.CiRateBucket
import com.inso_world.binocular.model.metrics.CiRatePerModule
import com.inso_world.binocular.model.metrics.FileComplexityMinorContributors

interface RepositoryInfrastructurePort : BinocularInfrastructurePort<Repository, Repository.Id> {
    fun findByName(name: String): Repository?

    fun findExistingCommits(
        repo: Repository,
        shas: Set<String>
    ): Sequence<Commit>

    fun findBranch(
        repository: Repository,
        name: String
    ): Branch?

    /**
     * Aggregates CI build results (failed / completed) per time bucket, for the timeline metric.
     *
     * A build only counts if its status is "failed" or "success". Buckets are labelled by the
     * database using [fmt], so [fmt] must match the label format the caller uses.
     *
     * @param repository the repository to look at (may be null = not scoped to a repo)
     * @param since      start of the range as epoch millis (UTC), aligned to the first period start
     * @param until      end of the range as epoch millis (UTC), aligned to the last period end
     * @param fmt        ArangoDB date format for the bucket label, e.g. "%mm/%yyyy" or "%yyyy"
     * @return one entry per bucket that has builds; never null, may be empty
     */
    fun findCiErrorRateBuckets(
        repository: Repository?, since: Long, until: Long, fmt: String
    ): List<CiRateBucket>

    /**
     * Counts commits per author, grouped into time buckets, for the timeline bus factor.
     *
     * Because the bus factor is cumulative over time, this returns per-bucket counts that the
     * caller adds up while walking through the periods. Commits older than [start] are folded
     * into the very first bucket ([firstLabel]) so that early history still counts.
     *
     * @param repository the repository to look at (may be null = not scoped to a repo)
     * @param until      upper bound as epoch millis (UTC); commits after this are ignored
     * @param start      start of the first period as epoch millis (UTC); commits before it go into [firstLabel]
     * @param firstLabel label of the first bucket, used for the "before start" commits
     * @param fmt        ArangoDB date format for the bucket label, e.g. "%mm/%yyyy" or "%yyyy"
     * @return one entry per (bucket, author); never null, may be empty
     */
    fun findAuthorCommitCountsByPeriod(
        repository: Repository?, until: Long, start: Long, firstLabel: String, fmt: String
    ): List<AuthorPeriodCount>

    fun findFileComplexityForAllFiles(
        repository: Repository?,
    ): Sequence<FileComplexityMinorContributors>

    /**
     * Counts commits per author for each module, over the WHOLE history (no time filter).
     *
     * Includes every author so the caller can work out each author's share of a module.
     * This is the input for the per-module bus factor.
     *
     * @param repository    the repository to look at (may be null = not scoped to a repo)
     * @param neededModules module paths to include; an empty list means "all modules"
     * @return one entry per (module, author); never null, may be empty
     */
    fun countCommitsByModule(
        repository: Repository?,
        neededModules: List<String>
    ): Sequence<AuthorCountPerModule>

    /**
     * Aggregates CI build results (failed / completed) per module within a time window.
     *
     * A build only counts if its status is "failed" or "success", and each build is counted once
     * even if several commits of the module link to it. Modules without builds in the window are omitted.
     *
     * @param repository    the repository to look at (may be null = not scoped to a repo)
     * @param since         start of the time window (epoch millis, UTC)
     * @param until         end of the time window (epoch millis, UTC)
     * @param neededModules module paths to include; an empty list means "all modules"
     * @return one entry per module that has builds in the window; never null, may be empty
     */
    fun findCiErrorRateByModule(
        repository: Repository?,
        since: Long,
        until: Long,
        neededModules: List<String>
    ): Sequence<CiRatePerModule>
}
