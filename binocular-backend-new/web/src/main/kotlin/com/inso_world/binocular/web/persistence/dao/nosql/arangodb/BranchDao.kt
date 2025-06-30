package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.persistence.dao.interfaces.IBranchDao
import com.inso_world.binocular.web.persistence.entity.arangodb.BranchEntity
import com.inso_world.binocular.web.persistence.mapper.arangodb.BranchMapper
import com.inso_world.binocular.web.persistence.repository.arangodb.BranchRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IBranchDao using the MappedArangoDbDao approach.
 * 
 * This class extends MappedArangoDbDao to leverage the entity mapping pattern,
 * which provides a clean separation between domain models (Branch) and 
 * database-specific entities (BranchEntity).
 */
@Repository
@Profile("nosql", "arangodb")
class BranchDao(
  @Autowired branchRepository: BranchRepository,
  @Autowired branchMapper: BranchMapper
) : MappedArangoDbDao<Branch, BranchEntity, String>(branchRepository, branchMapper), IBranchDao {
}
