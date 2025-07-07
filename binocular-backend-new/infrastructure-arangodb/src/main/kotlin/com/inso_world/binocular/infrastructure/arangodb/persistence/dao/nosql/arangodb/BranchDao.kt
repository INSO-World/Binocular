package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IBranchDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.BranchMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.BranchRepository
import com.inso_world.binocular.model.Branch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IBranchDao using the MappedArangoDbDao approach.
 *
 * This class extends MappedArangoDbDao to leverage the entity mapping pattern,
 * which provides a clean separation between domain models (Branch) and
 * database-specific entities (BranchEntity).
 */

@Repository
class BranchDao(
    @Autowired branchRepository: BranchRepository,
    @Autowired branchMapper: BranchMapper,
) : MappedArangoDbDao<Branch, BranchEntity, String>(branchRepository, branchMapper),
    IBranchDao
