package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IBranchDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.BranchMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.BranchRepository
import com.inso_world.binocular.model.Branch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IBranchDao using the MappedArangoDbDao approach.
 *
 * This class extends MappedArangoDbDao to leverage the entity mapping pattern,
 * which provides a clean separation between domain models (Branch) and
 * database-specific entities (BranchEntity).
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

    override fun findByName(name: String): Branch? {
        seeder.seed()
        return branchRepository.findByName(name)?.let { entity ->
            commitMapper.toDomain(entity.head)
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
                commitMapper.toDomain(entity.head)
                branchMapper.toDomain(entity)
            }
        val total = repository.count()
        return Page(content, total, pageable)
    }
}
