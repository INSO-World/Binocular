package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.infrastructure.arangodb.assembler.RepositoryAssembler
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.ICommitDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.CommitRepository
import com.inso_world.binocular.model.Commit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of [ICommitDao].
 *
 * All read methods pre-seed the [com.inso_world.binocular.core.persistence.mapper.context.MappingContext]
 * with each commit's owning repository (via [RepositoryAssembler]) before
 * delegating to [CommitMapper]. This satisfies [CommitMapper]'s invariant
 * that a [com.inso_world.binocular.model.Repository] is registered in
 * context prior to mapping the commit.
 */
@Repository
internal class CommitDao(
    @Autowired private val commitRepository: CommitRepository,
    @Autowired private val commitMapper: CommitMapper,
) : MappedArangoDbDao<Commit, CommitEntity, String>(commitRepository, commitMapper),
    ICommitDao {
    @Autowired
    private lateinit var seeder: DefaultMappingContextSeeder

    @Autowired
    @Lazy
    private lateinit var repositoryAssembler: RepositoryAssembler

    override fun findById(id: String): Commit? {
        seeder.seed()
        val entity = commitRepository.findById(id).orElse(null) ?: return null
        repositoryAssembler.toDomain(entity.repository)
        return commitMapper.toDomain(entity)
    }

    override fun findAll(): Iterable<Commit> {
        seeder.seed()
        return commitRepository.findAll().map { entity ->
            repositoryAssembler.toDomain(entity.repository)
            commitMapper.toDomain(entity)
        }
    }

    override fun findAll(pageable: Pageable): Page<Commit> {
        seeder.seed()
        val result = commitRepository.findAll(pageable)
        val content =
            result.content.map { entity ->
                repositoryAssembler.toDomain(entity.repository)
                commitMapper.toDomain(entity)
            }
        return Page(content, result.totalElements, pageable)
    }

    override fun findByRepositoryAndShaIn(
        repoPath: String,
        shas: Collection<String>
    ): Iterable<Commit> =
        commitRepository.findByRepositoryAndShaIn(repoPath, shas).map {
            commitMapper.toDomain(it)
        }
}
