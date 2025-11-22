package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.core.persistence.exception.PersistenceException
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.repository.CommitRepository
import com.inso_world.binocular.model.Commit
import jakarta.persistence.criteria.JoinType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Repository
import java.util.stream.Stream
import kotlin.uuid.ExperimentalUuidApi

/**
 * SQL implementation of ICommitDao.
 */
@Repository
internal class CommitDao(
    @field:Autowired
    private val repo: CommitRepository,
) : SqlDao<CommitEntity, Long>(),
    ICommitDao {
    init {
        this.setClazz(CommitEntity::class.java)
        this.setRepository(repo)
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun findByIid(iid: com.inso_world.binocular.model.Commit.Id): CommitEntity? = repo.findByIid(iid.value)

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
        repository: com.inso_world.binocular.model.Repository,
        shas: List<String>,
    ): Iterable<CommitEntity> {
        val rid = repository.id ?: throw PersistenceException("Cannot search for repo without valid ID")
        val shas =
            this.repo.findAll(
                Specification.allOf(
                    CommitEntitySpecification
                        .hasRepositoryId(rid.toLong())
                        .and(CommitEntitySpecification.hasShaIn(shas)),
                ),
            )
        return shas
    }

    override fun findHeadForBranch(
        repository: com.inso_world.binocular.model.Repository,
        branch: String,
    ): CommitEntity? {
        val rid = repository.id
        if (rid == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findLeafCommitsByRepository(rid.toLong(), branch)
    }

    override fun findAllLeafCommits(repository: com.inso_world.binocular.model.Repository): Iterable<CommitEntity> {
        val rid = repository.id
        if (rid == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findAllLeafCommits(rid.toLong())
    }

    override fun findBySha(
        repository: RepositoryEntity,
        sha: String,
    ): CommitEntity? {
        val rid = repository.id
        if (rid == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findByRepository_IdAndSha(rid, sha)
    }

    override fun findAll(repository: com.inso_world.binocular.model.Repository): Stream<CommitEntity> {
        val rid = repository.id
        if (rid == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findAllByRepository_Id(rid.toLong())
    }

    override fun findAllAsStream(): Stream<CommitEntity> = repo.findAllAsStream()

    override fun findParentsBySha(sha: String): Set<CommitEntity> = this.repo.findParentsBySha(sha)

    override fun findChildrenBySha(sha: String): Set<CommitEntity> = this.repo.findChildrenBySha(sha)
}
