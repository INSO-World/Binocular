package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.infrastructure.arangodb.assembler.RepositoryAssembler
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IBranchDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.BranchMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.BranchRepository
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Reference
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import kotlin.uuid.ExperimentalUuidApi

/**
 * ArangoDB implementation of [IBranchDao] using the [MappedArangoDbDao] approach.
 *
 * All read methods pre-seed the [com.inso_world.binocular.core.mapping.context.MappingContext]
 * with the branch's owning repository (via [RepositoryAssembler]) and its head commit
 * (via [CommitMapper]) before delegating to [BranchMapper].  This satisfies [BranchMapper]'s
 * invariant that both a [com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity]
 * and a [com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CommitEntity] are
 * registered in context prior to mapping the branch.
 */
@Repository
internal class BranchDao(
    @Autowired private val branchRepository: BranchRepository,
    @Autowired private val branchMapper: BranchMapper,
    @Autowired private val commitMapper: CommitMapper,
) : MappedArangoDbDao<Branch, BranchEntity, String>(branchRepository, branchMapper),
    IBranchDao {
    @Autowired
    private lateinit var seeder: DefaultMappingContextSeeder

    @Autowired
    @Lazy
    private lateinit var repositoryAssembler: RepositoryAssembler

    override fun findById(id: String): Branch? {
        seeder.seed()
        val entity = branchRepository.findById(id).orElse(null) ?: return null
        repositoryAssembler.toDomain(entity.repository)
        commitMapper.toDomain(entity.head)
        return branchMapper.toDomain(entity)
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun findByIid(id: Reference.Id): Branch? {
        seeder.seed()
        val entity = branchRepository.findByIid(id.value) ?: return null
        repositoryAssembler.toDomain(entity.repository)
        commitMapper.toDomain(entity.head)
        return branchMapper.toDomain(entity)
    }

    override fun findByName(name: String): Branch? {
        seeder.seed()
        return branchRepository.findByName(name)?.let { entity ->
            repositoryAssembler.toDomain(
                entity.repository
            )
            commitMapper.toDomain(
                entity.head
            )
            branchMapper.toDomain(entity)
        }
    }

    override fun findAll(pageable: Pageable): Page<Branch> {
        seeder.seed()
        val offset = pageable.offset.toInt()
        val limit = pageable.pageSize
        val firstOrder = pageable.sort.firstOrNull()
        val asc = firstOrder?.direction == Sort.Direction.ASC

        val entities =
            if (asc) {
                branchRepository.findAllSortedAsc(offset, limit)
            } else {
                branchRepository.findAllSortedDesc(offset, limit)
            }
        val content =
            entities.map { entity ->
                repositoryAssembler.toDomain(
                    entity.repository
                        ?: throw IllegalStateException("BranchEntity.repository not loaded from ArangoDB — @Ref field was null."),
                )
                commitMapper.toDomain(
                    entity.head
                        ?: throw IllegalStateException("BranchEntity.head not loaded from ArangoDB — @Ref field was null."),
                )
                branchMapper.toDomain(entity)
            }
        val total = repository.count()
        return Page(content, total, pageable)
    }
}
