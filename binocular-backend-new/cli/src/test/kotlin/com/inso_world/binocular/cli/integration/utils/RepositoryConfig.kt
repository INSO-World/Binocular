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
    val startCommit: String,
    val hashes: List<Commit>,
    val project: Project,
)

internal fun setupRepoConfig(
    path: String,
    startSha: String? = "HEAD",
    branch: Branch,
    projectName: String,
): RepositoryConfig {
    val ffi = BinocularFfi()
    val repo = run {
        val p = Path(path)
        return@run ffi.findRepo(p)
    }
    require(repo.branches.add(branch))
    val cmt = ffi.findCommit(repo, startSha ?: "HEAD")
    val hashes = ffi.traverseBranch(repo,branch)
    val project =
        Project(
            name = projectName,
        )
    project.repo = repo
    repo.project = project
    return RepositoryConfig(
        repo = repo,
        startCommit = cmt,
        hashes = hashes,
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
