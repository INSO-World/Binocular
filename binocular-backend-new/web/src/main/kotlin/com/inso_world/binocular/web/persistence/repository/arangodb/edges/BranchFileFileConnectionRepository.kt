package com.inso_world.binocular.web.persistence.repository.arangodb.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.edge.BranchFileFileConnection
import org.springframework.stereotype.Repository

@Repository
interface BranchFileFileConnectionRepository: ArangoRepository<BranchFileFileConnection, String> {

  @Query("""
    FOR c IN `branch-files-files`
        FILTER c._from == CONCAT('files/', @branchFileId)
        FOR f IN files
            FILTER f._id == c._to
            RETURN f
""")
  fun findFilesByBranchFile(branchFileId: String): List<File>

  @Query("""
    FOR c IN `branch-files-files`
        FILTER c._to == CONCAT('files/', @fileId)
        FOR f IN files
            FILTER f._id == c._from
            RETURN f
""")
  fun findBranchFilesByFile(fileId: String): List<File>
}
