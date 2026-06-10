package com.inso_world.binocular.infrastructure.arangodb.persistence.repository

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Repository
interface ProjectRepository : ArangoRepository<ProjectEntity, String> {
    fun findByName(name: String): ProjectEntity?

    @OptIn(ExperimentalUuidApi::class)
    @Query("FOR p IN projects FILTER p.iid == @iid LIMIT 1 RETURN p")
    fun findByIid(
        @Param("iid") iid: Uuid
    ): ProjectEntity?
}
