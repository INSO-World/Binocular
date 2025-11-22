package com.inso_world.binocular.cli.integration.utils

import com.inso_world.binocular.cli.service.RepositoryService
import com.inso_world.binocular.ffi.BinocularFfi
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import kotlin.io.path.Path

internal data class RepositoryConfig(
    val repo: Repository,
    val startCommit: Commit,
    val hashes: List<Commit>,
    val project: Project,
)

internal fun setupRepoConfig(
    path: String,
    startSha: String? = "HEAD",
    branchName: String,
    projectName: String,
): RepositoryConfig {
    val ffi = BinocularFfi()
    val project =
        Project(
            name = projectName,
        )
    val repo = run {
        val p = Path(path)
        return@run ffi.findRepo(p, project)
    }
    val (branch, commits) = ffi.traverseBranch(repo, branchName)
    val cmt = ffi.findCommit(repo, startSha ?: "HEAD")
    return RepositoryConfig(
        repo = repo,
        startCommit = cmt,
        hashes = commits,
        project = project,
    )
}

@Deprecated("legacy")
internal fun generateCommits(
    svc: RepositoryService,
    repoConfig: RepositoryConfig,
    concreteRepo: Repository,
): List<Commit> =
    svc.transformCommits(
        concreteRepo,
        repoConfig.hashes
    ).toList()
