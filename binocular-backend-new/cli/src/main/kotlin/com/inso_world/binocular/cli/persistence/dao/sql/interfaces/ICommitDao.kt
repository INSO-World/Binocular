package com.inso_world.binocular.cli.persistence.dao.sql.interfaces

import com.inso_world.binocular.cli.entity.Commit
import com.inso_world.binocular.cli.entity.Repository
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IDao
import org.springframework.data.domain.Pageable

interface ICommitDao : IDao<Commit, String> {
    fun findExistingSha(
        repo: Repository,
        shas: List<String>,
    ): Set<Commit>

    fun findAllByRepo(
        repo: Repository,
        pageable: Pageable,
    ): Iterable<Commit>

    // TODO branch should be required!
    fun findHeadForBranch(
        repo: Repository,
        branch: String,
    ): Commit?

    fun findAllLeafCommits(repo: Repository): Iterable<Commit>
}
