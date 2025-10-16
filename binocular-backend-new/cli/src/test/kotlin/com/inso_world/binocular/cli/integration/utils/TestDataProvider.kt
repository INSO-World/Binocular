package com.inso_world.binocular.cli.integration.utils

import com.inso_world.binocular.core.index.GitIndexer
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import java.time.LocalDateTime
import kotlin.io.path.Path

internal class RealDataProvider(
    private val idx: GitIndexer,

    ) {
    private lateinit var repo: Repository
    lateinit var project: Project

    fun setUp(
        projectName: String,
        repoPath: String,
        startSha: String? = "HEAD",
        branch: Branch,
    ) {
        project = Project(
            name = projectName,
        )
        repo = idx.findRepo(Path(repoPath).toRealPath())
        project.repo = repo
        repo.project = project

        require(repo.branches.add(branch))
//        val cmt = idx.findCommit(repo, startSha ?: "HEAD")
        idx.traverseBranch(repo,branch)
    }
}
