package com.inso_world.binocular.web.persistence.repository.arangodb.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.persistence.entity.arangodb.CommitEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.CommitModuleConnectionEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.ModuleEntity
import org.springframework.stereotype.Repository

@Repository
interface CommitModuleConnectionRepository: ArangoRepository<CommitModuleConnectionEntity, String> {

  @Query("""
    FOR c IN `commits-modules`
        FILTER c._from == CONCAT('commits/', @commitId)
        FOR m IN modules
            FILTER m._id == c._to
            RETURN m
""")
  fun findModulesByCommit(commitId: String): List<ModuleEntity>

  @Query("""
    FOR c IN `commits-modules`
        FILTER c._to == CONCAT('modules/', @moduleId)
        FOR cm IN commits
            FILTER cm._id == c._from
            RETURN cm
""")
  fun findCommitsByModule(moduleId: String): List<CommitEntity>
}
