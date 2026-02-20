    package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

    import com.inso_world.binocular.core.service.exception.NotFoundException
    import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.ICommitDao
    import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CommitEntity
    import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.CommitMapper
    import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.CommitRepository
    import com.inso_world.binocular.model.Commit
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
    internal class CommitDao
        @Autowired
        constructor(
            private val commitRepository: CommitRepository,
            private val commitMapper: CommitMapper,
    ) : MappedArangoDbDao<Commit, CommitEntity, String>(commitRepository, commitMapper),
        ICommitDao {



        override fun findBySha(sha: String): Commit? {
            seeder.seed()
            val commitEntity: CommitEntity? = commitRepository.findBySha(sha)

            // 2. Use a sensible failure method if null
            return commitEntity?.let { commitMapper.toDomain(it) }
                ?: return null
        }
    }
