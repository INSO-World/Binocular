package com.inso_world.binocular.cli.persistence.dao.sql

import com.inso_world.binocular.cli.entity.Commit
import com.inso_world.binocular.cli.exception.PersistenceException
import com.inso_world.binocular.cli.persistence.dao.sql.interfaces.ICommitDao
import com.inso_world.binocular.cli.persistence.repository.sql.CommitRepository
import com.inso_world.binocular.infrastructure.sql.persistence.dao.SqlDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository("cliCommitDao")
class CommitDao(
    @Autowired private val commitRepository: CommitRepository,
) : SqlDao<Commit, String>(),
    ICommitDao {
    init {
        this.setClazz(Commit::class.java)
        this.setRepository(commitRepository)
    }

    override fun findExistingSha(
        repo: com.inso_world.binocular.cli.entity.Repository,
        shas: List<String>,
    ): Set<Commit> {
        if (repo.id == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.commitRepository.findAllByRepository_IdAndShaIn(repo.id!!, shas)
    }

    override fun findAllByRepo(
        repo: com.inso_world.binocular.cli.entity.Repository,
        pageable: Pageable,
    ): Iterable<Commit> {
        if (repo.id == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.commitRepository.findAllByRepository_Id(repo.id!!)
    }

    override fun findHeadForBranch(
        repo: com.inso_world.binocular.cli.entity.Repository,
        branch: String,
    ): Commit? {
        if (repo.id == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.commitRepository.findLeafCommitsByRepository(repo.id!!, branch)
    }

    override fun findAllLeafCommits(repo: com.inso_world.binocular.cli.entity.Repository): Iterable<Commit> {
        if (repo.id == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.commitRepository.findAllLeafCommits(repo.id!!)
    }
}
