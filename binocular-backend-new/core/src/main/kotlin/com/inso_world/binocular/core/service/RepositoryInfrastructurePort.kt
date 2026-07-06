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

    fun findCiErrorRateBuckets(
        repository: Repository?, since: Long, until: Long, fmt: String
    ): List<CiRateBucket>

    fun findAuthorCommitCountsByPeriod(
        repository: Repository?, until: Long, start: Long, firstLabel: String, fmt: String
    ): List<AuthorPeriodCount>

    fun findFileComplexityForAllFiles(
        repository: Repository?,
    ): Sequence<FileComplexityMinorContributors>

    fun countCommitsByModule(
        repository: Repository?,
        neededModules: List<String>
    ): Sequence<AuthorCountPerModule>

    fun findCiErrorRateByModule(
        repository: Repository?,
        since: Long,
        until: Long,
        neededModules: List<String>
    ): Sequence<CiRatePerModule>
}
