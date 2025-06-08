package com.inso_world.binocular.web.persistence.repository.arangodb.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.entity.Build
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.edge.CommitBuildConnection
import org.springframework.stereotype.Repository

@Repository
interface CommitBuildConnectionRepository: ArangoRepository<CommitBuildConnection, String> {

  @Query("""
    FOR c IN `commits-builds`
        FILTER c._from == CONCAT('commits/', @commitId)
        FOR b IN builds
            FILTER b._id == c._to
            RETURN b
""")
  fun findBuildsByCommit(commitId: String): List<Build>

  @Query("""
    FOR c IN `commits-builds`
        FILTER c._to == CONCAT('builds/', @buildId)
        FOR cm IN commits
            FILTER cm._id == c._from
            RETURN cm
""")
  fun findCommitsByBuild(buildId: String): List<Commit>
}
