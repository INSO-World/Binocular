package com.inso_world.binocular.web.persistence.repository.arangodb.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.edge.CommitFileConnection
import org.springframework.stereotype.Repository

@Repository
interface CommitFileConnectionRepository: ArangoRepository<CommitFileConnection, String> {

  @Query("""
    FOR c IN `commits-files`
        FILTER c._from == CONCAT('commits/', @commitId)
        FOR f IN files
            FILTER f._id == c._to
            RETURN f
""")
  fun findFilesByCommit(commitId: String): List<File>

  @Query("""
    FOR c IN `commits-files`
        FILTER c._to == CONCAT('files/', @fileId)
        FOR cm IN commits
            FILTER cm._id == c._from
            RETURN cm
""")
  fun findCommitsByFile(fileId: String): List<Commit>
}
