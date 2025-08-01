package com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.FileEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.BranchFileConnectionEntity
import org.springframework.stereotype.Repository

@Repository
interface BranchFileConnectionRepository : ArangoRepository<BranchFileConnectionEntity, String> {
    @Query(
        """
    FOR c IN `branches-files`
        FILTER c._from == CONCAT('branches/', @branchId)
        FOR f IN files
            FILTER f._id == c._to
            RETURN f
""",
    )
    fun findFilesByBranch(branchId: String): List<FileEntity>

    @Query(
        """
    FOR c IN `branches-files`
        FILTER c._to == CONCAT('files/', @fileId)
        FOR b IN branches
            FILTER b._id == c._from
            RETURN b
""",
    )
    fun findBranchesByFile(fileId: String): List<BranchEntity>
}
