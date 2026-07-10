package com.inso_world.binocular.infrastructure.arangodb.persistence.repository

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Repository
interface RepositoryRepository : ArangoRepository<RepositoryEntity, String> {
    fun findByLocalPath(localPath: String): RepositoryEntity?

    @Suppress("ktlint:standard:function-naming")
    fun findByProject_Name(projectName: String): RepositoryEntity?

    @OptIn(ExperimentalUuidApi::class)
    @Query("FOR r IN repositories FILTER r.iid == @iid LIMIT 1 RETURN r")
    fun findByIid(
        @Param("iid") iid: Uuid
    ): RepositoryEntity?
}
