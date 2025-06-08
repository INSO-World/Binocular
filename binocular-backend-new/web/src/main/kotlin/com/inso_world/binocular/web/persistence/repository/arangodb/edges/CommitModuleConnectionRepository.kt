package com.inso_world.binocular.web.persistence.repository.arangodb.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.entity.edge.CommitModuleConnection
import org.springframework.stereotype.Repository

@Repository
interface CommitModuleConnectionRepository: ArangoRepository<CommitModuleConnection, String> {

  @Query("""
    FOR c IN `commits-modules`
        FILTER c._from == CONCAT('commits/', @commitId)
        FOR m IN modules
            FILTER m._id == c._to
            RETURN m
""")
  fun findModulesByCommit(commitId: String): List<Module>

  @Query("""
    FOR c IN `commits-modules`
        FILTER c._to == CONCAT('modules/', @moduleId)
        FOR cm IN commits
            FILTER cm._id == c._from
            RETURN cm
""")
  fun findCommitsByModule(moduleId: String): List<Commit>
}
