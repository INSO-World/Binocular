package com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.FileEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.BranchFileFileConnectionEntity
import org.springframework.stereotype.Repository

@Repository
interface BranchFileFileConnectionRepository : ArangoRepository<BranchFileFileConnectionEntity, String> {
    @Query(
        """
    FOR c IN `branch-files-files`
        FILTER c._from == CONCAT('files/', @branchFileId)
        FOR f IN files
            FILTER f._id == c._to
            RETURN f
""",
    )
    fun findFilesByBranchFile(branchFileId: String): List<FileEntity>

    @Query(
        """
    FOR c IN `branch-files-files`
        FILTER c._to == CONCAT('files/', @fileId)
        FOR f IN files
            FILTER f._id == c._from
            RETURN f
""",
    )
    fun findBranchFilesByFile(fileId: String): List<FileEntity>
}
