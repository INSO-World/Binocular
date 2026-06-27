package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.IDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import com.inso_world.binocular.model.Project
import kotlin.uuid.ExperimentalUuidApi

internal interface IProjectDao : IDao<Project, String> {
    fun findAllEntities(): Iterable<ProjectEntity>

    fun findByName(name: String): Project?

    @OptIn(ExperimentalUuidApi::class)
    @Deprecated("Use findEntityByIid() instead")
    fun findByIid(iid: Project.Id): Project?

    @OptIn(ExperimentalUuidApi::class)
    fun findEntityByIid(iid: Project.Id): ProjectEntity?
}
