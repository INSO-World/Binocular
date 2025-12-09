package com.inso_world.binocular.core.service

import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.CommitDiff
import com.inso_world.binocular.model.Repository

interface RepositoryInfrastructurePort : BinocularInfrastructurePort<Repository, Repository.Id> {
    fun findByName(name: String): Repository?

    fun findExistingCommits(repo: Repository, shas: Set<String>): Sequence<Commit>

    fun saveCommitDiffs(
        repository: Repository,
        diffs: Set<CommitDiff>,
    ): Set<CommitDiff>

    fun findAllDiffs(repository: Repository): Set<CommitDiff>
    fun findBranch(repository: Repository, name: String): Branch?
//    fun findAllBranches(repository: Repository): Iterable<Branch>
//
//    fun findAllCommits(repository: Repository): Iterable<Commit>
}
