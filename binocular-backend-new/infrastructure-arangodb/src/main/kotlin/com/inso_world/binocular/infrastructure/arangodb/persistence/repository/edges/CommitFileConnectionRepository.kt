package com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.FileEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitFileConnectionEntity
import org.springframework.stereotype.Repository

@Repository
interface CommitFileConnectionRepository : ArangoRepository<CommitFileConnectionEntity, String> {
    @Query(
        """
    FOR c IN `commits-files`
        FILTER c._from == CONCAT('commits/', @commitId)
        FOR f IN files
            FILTER f._id == c._to
            RETURN f
""",
    )
    fun findFilesByCommit(commitId: String): List<FileEntity>

    @Query(
        """
    FOR c IN `commits-files`
        FILTER c._to == CONCAT('files/', @fileId)
        FOR cm IN commits
            FILTER cm._id == c._from
            RETURN cm
""",
    )
    fun findCommitsByFile(fileId: String): List<CommitEntity>
}
