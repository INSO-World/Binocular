package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.core.persistence.exception.PersistenceException
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.repository.CommitRepository
import jakarta.persistence.criteria.JoinType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Repository
import java.util.stream.Stream
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
        @OptIn(ExperimentalUuidApi::class)
        fun hasRepositoryIid(iid: com.inso_world.binocular.model.Repository.Id): Specification<CommitEntity> =
            Specification { root, query, cb ->
                if (query?.resultType != Long::class.java) {
                    root.fetch<CommitEntity, Any>("committer", JoinType.LEFT)
                    root.fetch<CommitEntity, Any>("author", JoinType.LEFT)
                }
                cb.equal(root.get<RepositoryEntity>("repository").get<Uuid>("iid"), iid.value)
            }

        fun hasShaIn(shas: Collection<String>): Specification<CommitEntity> =
            Specification { root, _, cb ->
                root.get<String>("sha").`in`(shas)
            }
    }

    override fun findExistingSha(
        repository: com.inso_world.binocular.model.Repository,
        shas: Collection<String>,
    ): Iterable<CommitEntity> {
        val rid = repository.iid
        val shas =
            this.repo.findAll(
                Specification.allOf(
                    CommitEntitySpecification
                        .hasRepositoryIid(rid)
                        .and(CommitEntitySpecification.hasShaIn(shas)),
                ),
            )
        return shas
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun findHeadForBranch(
        repository: RepositoryEntity,
        branch: String,
    ): CommitEntity? {
        return this.repo.findLeafCommitsByRepository(repository.iid.value, branch)
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun findAllLeafCommits(repository: RepositoryEntity): Iterable<CommitEntity> {
        return this.repo.findAllLeafCommits(repository.iid.value)
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun findBySha(
        repository: RepositoryEntity,
        sha: String,
    ): CommitEntity? {
        return this.repo.findByRepository_IidAndSha(repository.iid.value, sha)
    }

    override fun findAll(repository: com.inso_world.binocular.model.Repository): Stream<CommitEntity> {
        return this.repo.findAllByRepository_Iid(repository.iid)
    }

    override fun findAllAsStream(): Stream<CommitEntity> = repo.findAllAsStream()

    override fun findParentsBySha(sha: String): Set<CommitEntity> = this.repo.findParentsBySha(sha)

    override fun findChildrenBySha(sha: String): Set<CommitEntity> = this.repo.findChildrenBySha(sha)
}
