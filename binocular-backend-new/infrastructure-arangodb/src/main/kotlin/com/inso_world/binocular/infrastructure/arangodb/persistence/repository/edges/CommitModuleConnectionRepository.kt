package com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ModuleEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitModuleConnectionEntity
import org.springframework.stereotype.Repository

@Repository
interface CommitModuleConnectionRepository : ArangoRepository<CommitModuleConnectionEntity, String> {
    @Query(
        """
    FOR c IN `commits-modules`
        FILTER c._from == CONCAT('commits/', @commitId)
        FOR m IN modules
            FILTER m._id == c._to
            RETURN m
""",
    )
    fun findModulesByCommit(commitId: String): List<ModuleEntity>

    @Query(
        """
    FOR c IN `commits-modules`
        FILTER c._to == CONCAT('modules/', @moduleId)
        FOR cm IN commits
            FILTER cm._id == c._from
            RETURN cm
""",
    )
    fun findCommitsByModule(moduleId: String): List<CommitEntity>
}
