package com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces

import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import org.springframework.data.domain.Pageable

internal interface ICommitDao : IDao<CommitEntity, Long> {
    fun findExistingSha(
        repo: RepositoryEntity,
        shas: List<String>,
    ): Set<CommitEntity>

    fun findAllByRepo(
        repo: RepositoryEntity,
        pageable: Pageable,
    ): Iterable<CommitEntity>

    // TODO branch should be required!
    fun findHeadForBranch(
        repo: RepositoryEntity,
        branch: String,
    ): CommitEntity?

    fun findAllLeafCommits(repo: RepositoryEntity): Iterable<CommitEntity>
}
