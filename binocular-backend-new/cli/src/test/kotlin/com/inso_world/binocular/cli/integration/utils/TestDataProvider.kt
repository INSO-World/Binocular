package com.inso_world.binocular.cli.integration.utils

import com.inso_world.binocular.core.index.GitIndexer
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import kotlin.io.path.Path

internal class RealDataProvider(
    private val idx: GitIndexer,

    ) {
    private lateinit var repo: Repository
    lateinit var project: Project

    fun setUp(
        projectName: String,
        repoPath: String,
        branchName: String,
    ) {
        project = Project(
            name = projectName,
        )
        repo = idx.findRepo(Path(repoPath).toRealPath(), project)
        project.repo = repo

        idx.traverseBranch(repo, branchName)
    }
}
