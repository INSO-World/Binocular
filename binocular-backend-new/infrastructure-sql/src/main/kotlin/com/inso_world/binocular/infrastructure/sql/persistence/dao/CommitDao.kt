package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.core.persistence.exception.PersistenceException
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.repository.CommitRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.validation.annotation.Validated

/**
 * SQL implementation of ICommitDao.
 */
@Repository
@Validated
internal class CommitDao(
    @Autowired
    private val repo: CommitRepository,
) : SqlDao<CommitEntity, Long>(),
    ICommitDao {
    init {
        this.setClazz(CommitEntity::class.java)
        this.setRepository(repo)
    }

    override fun findExistingSha(
        repo: RepositoryEntity,
        shas: List<String>,
    ): Set<CommitEntity> {
        if (repo.id == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findAllByRepository_IdAndShaIn(repo.id!!, shas)
    }

    override fun findAllByRepo(
        repo: RepositoryEntity,
        pageable: Pageable,
    ): Iterable<CommitEntity> {
        if (repo.id == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findAllByRepository_Id(repo.id!!)
    }

    override fun findHeadForBranch(
        repo: RepositoryEntity,
        branch: String,
    ): CommitEntity? {
        if (repo.id == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findLeafCommitsByRepository(repo.id!!, branch)
    }

    override fun findAllLeafCommits(repo: RepositoryEntity): Iterable<CommitEntity> {
        if (repo.id == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findAllLeafCommits(repo.id!!)
    }
}
