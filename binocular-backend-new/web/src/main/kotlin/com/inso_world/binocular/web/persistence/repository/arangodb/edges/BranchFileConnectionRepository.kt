package com.inso_world.binocular.web.persistence.repository.arangodb.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.persistence.entity.arangodb.BranchEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.BranchFileConnectionEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.FileEntity
import org.springframework.stereotype.Repository

@Repository
interface BranchFileConnectionRepository: ArangoRepository<BranchFileConnectionEntity, String> {

  @Query("""
    FOR c IN `branches-files`
        FILTER c._from == CONCAT('branches/', @branchId)
        FOR f IN files
            FILTER f._id == c._to
            RETURN f
""")
  fun findFilesByBranch(branchId: String): List<FileEntity>

  @Query("""
    FOR c IN `branches-files`
        FILTER c._to == CONCAT('files/', @fileId)
        FOR b IN branches
            FILTER b._id == c._from
            RETURN b
""")
  fun findBranchesByFile(fileId: String): List<BranchEntity>

}
