package com.inso_world.binocular.core.index

import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.CommitDiff
import com.inso_world.binocular.model.Repository
import jakarta.validation.Valid
import java.nio.file.Path

interface GitIndexer {
    fun findRepo(path: Path): Repository

    fun traverseBranch(
        repo: Repository,
        branch: Branch,
    ): List<Commit>

    fun findAllBranches(repo: Repository): List<Branch>

    fun findCommit(
        repo: Repository,
        hash: String,
    ): String

    fun traverse(
        repo: Repository,
        sourceCmt: String,
        trgtCmt: String? = null,
    ): List<Commit>

}
