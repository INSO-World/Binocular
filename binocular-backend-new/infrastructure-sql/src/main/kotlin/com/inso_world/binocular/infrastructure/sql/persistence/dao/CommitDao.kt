package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.core.persistence.exception.PersistenceException
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.repository.CommitRepository
import jakarta.persistence.criteria.JoinType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Repository

/**
 * SQL implementation of ICommitDao.
 */
@Repository
internal class CommitDao(
    @Autowired
    private val repo: CommitRepository,
) : SqlDao<CommitEntity, Long>(),
    ICommitDao {
    init {
        this.setClazz(CommitEntity::class.java)
        this.setRepository(repo)
    }

    private object CommitEntitySpecification {
        fun hasRepositoryId(repoId: Long): Specification<CommitEntity> =
            Specification { root, query, cb ->
                if (query?.resultType != Long::class.java) {
                    root.fetch<CommitEntity, Any>("committer", JoinType.LEFT)
                    root.fetch<CommitEntity, Any>("author", JoinType.LEFT)
                }
                cb.equal(root.get<RepositoryEntity>("repository").get<Long>("id"), repoId)
            }

        fun hasShaIn(shas: List<String>): Specification<CommitEntity> =
            Specification { root, _, cb ->
                root.get<String>("sha").`in`(shas)
            }
    }

    override fun findExistingSha(
        repo: RepositoryEntity,
        shas: List<String>,
    ): Iterable<CommitEntity> {
        if (repo.id == null) throw PersistenceException("Cannot search for repo without valid ID")
        val shas =
            this.repo.findAll(
                Specification.allOf(
                    CommitEntitySpecification
                        .hasRepositoryId(repo.id)
                        .and(CommitEntitySpecification.hasShaIn(shas)),
                ),
            )
        return shas
    }

    override fun findAllByRepo(
        repo: RepositoryEntity,
        pageable: Pageable,
    ): Iterable<CommitEntity> {
        if (repo.id == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findAllByRepository_Id(repo.id)
    }

    override fun findHeadForBranch(
        repo: RepositoryEntity,
        branch: String,
    ): CommitEntity? {
        if (repo.id == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findLeafCommitsByRepository(repo.id, branch)
    }

    override fun findAllLeafCommits(repo: RepositoryEntity): Iterable<CommitEntity> {
        if (repo.id == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findAllLeafCommits(repo.id)
    }

    override fun findBySha(
        repo: RepositoryEntity,
        sha: String,
    ): CommitEntity? {
        if (repo.id == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findByRepository_IdAndSha(repo.id, sha)
    }

    override fun findAll(repo: com.inso_world.binocular.model.Repository): Iterable<CommitEntity> {
        val rid = repo.id
        if (rid == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findAll(
            Specification.allOf(CommitEntitySpecification.hasRepositoryId(rid.toLong())),
        )
    }
}
