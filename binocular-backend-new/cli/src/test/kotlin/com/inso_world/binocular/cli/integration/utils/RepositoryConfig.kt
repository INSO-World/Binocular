package com.inso_world.binocular.cli.integration.utils

import com.inso_world.binocular.cli.entity.Commit
import com.inso_world.binocular.cli.entity.Repository
import com.inso_world.binocular.cli.index.vcs.toDto
import com.inso_world.binocular.cli.service.RepositoryService
import com.inso_world.binocular.ffi.BinocularFfi
import com.inso_world.binocular.ffi.pojos.BinocularCommitPojo
import com.inso_world.binocular.ffi.pojos.BinocularRepositoryPojo

internal data class RepositoryConfig(
    val repo: BinocularRepositoryPojo,
    val startCommit: String,
    val hashes: List<BinocularCommitPojo>,
)

internal fun setupRepoConfig(
    path: String,
    startSha: String? = "HEAD",
    branch: String = "master",
): RepositoryConfig {
    val ffi = BinocularFfi()
    val repo = ffi.findRepo(path)
    val cmt = ffi.findCommit(repo, startSha ?: "HEAD")
    val hashes = ffi.traverseBranch(repo, branch)
    return RepositoryConfig(
        repo = repo,
        startCommit = cmt,
        hashes = hashes,
    )
}

internal fun generateCommits(
    repoConfig: RepositoryConfig,
    concreteRepo: Repository,
): List<Commit> =
    RepositoryService()
        .transformCommits(
            concreteRepo,
            repoConfig.hashes.map {
                it.toDto()
            },
        ).toList()
