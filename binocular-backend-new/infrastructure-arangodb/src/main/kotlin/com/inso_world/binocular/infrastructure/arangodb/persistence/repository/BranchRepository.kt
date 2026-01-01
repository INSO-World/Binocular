package com.inso_world.binocular.infrastructure.arangodb.persistence.repository

import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.BranchEntity
import org.springframework.stereotype.Repository

@Repository
interface BranchRepository : ArangoRepository<BranchEntity, String> {

    fun findByBranch(branch: String): BranchEntity?

}
