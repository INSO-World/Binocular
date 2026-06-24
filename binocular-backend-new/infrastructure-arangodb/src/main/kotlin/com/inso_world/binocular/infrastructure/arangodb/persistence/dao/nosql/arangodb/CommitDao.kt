package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.ICommitDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CiRateBucketEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.AuthorCommitCountMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.CommitRepository
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.metrics.AuthorPeriodCount
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of ICommitDao using the MappedArangoDbDao approach.
 *
 * This class extends MappedArangoDbDao to leverage the entity mapping pattern,
 * which provides a clean separation between domain models (Commit) and
 * database-specific entities (CommitEntity).
 */
@Repository
internal class CommitDao(
    @Autowired private val commitRepository: CommitRepository,
    @Autowired private val commitMapper: CommitMapper,
    @Autowired private val authorCommitCountMapper: AuthorCommitCountMapper,
) : MappedArangoDbDao<Commit, CommitEntity, String>(commitRepository, commitMapper),
    ICommitDao {

    override fun findAuthorCommitCountsByPeriod(
        since: Long,
        until: Long,
        firstLabel: String,
        fmt: String
    ): List<AuthorPeriodCount> {
        return authorCommitCountMapper.toDomainList(
            commitRepository.findAuthorCommitCountsByPeriod(since, until, firstLabel, fmt)
        )
    }
}
