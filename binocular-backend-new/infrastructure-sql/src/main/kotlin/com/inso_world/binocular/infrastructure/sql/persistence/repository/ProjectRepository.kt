package com.inso_world.binocular.infrastructure.sql.persistence.repository

import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Repository
internal interface ProjectRepository : JpaRepository<ProjectEntity, Long> {
    fun findByName(name: String): ProjectEntity?

    @OptIn(ExperimentalUuidApi::class)
    fun findByIid(iid: Uuid): ProjectEntity?
}
