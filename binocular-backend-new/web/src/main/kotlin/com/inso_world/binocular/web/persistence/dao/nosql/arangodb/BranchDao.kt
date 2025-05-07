package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.persistence.dao.interfaces.IBranchDao
import com.inso_world.binocular.web.persistence.repository.arangodb.BranchRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class BranchDao(
  @Autowired private val branchRepository: BranchRepository
) : ArangoDbDao<Branch, String>(), IBranchDao {

  init {
    this.setClazz(Branch::class.java)
    this.setRepository(branchRepository)
  }
}
