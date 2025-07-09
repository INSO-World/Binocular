package com.inso_world.binocular.cli.integration.utils

import com.inso_world.binocular.cli.index.vcs.toDto
import com.inso_world.binocular.cli.service.RepositoryService
import com.inso_world.binocular.ffi.BinocularFfi
import com.inso_world.binocular.ffi.pojos.BinocularCommitPojo
import com.inso_world.binocular.ffi.pojos.BinocularRepositoryPojo
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository

internal data class RepositoryConfig(
    val repo: BinocularRepositoryPojo,
    val startCommit: String,
    val hashes: List<BinocularCommitPojo>,
    val project: Project,
)

internal fun setupRepoConfig(
    path: String,
    startSha: String? = "HEAD",
    branch: String = "master",
    projectName: String,
): RepositoryConfig {
    val ffi = BinocularFfi()
    val repo = ffi.findRepo(path)
    val cmt = ffi.findCommit(repo, startSha ?: "HEAD")
    val hashes = ffi.traverseBranch(repo, branch)
    val project =
        Project(
            id = null,
            name = projectName,
        )
    return RepositoryConfig(
        repo = repo,
        startCommit = cmt,
        hashes = hashes,
        project = project,
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
