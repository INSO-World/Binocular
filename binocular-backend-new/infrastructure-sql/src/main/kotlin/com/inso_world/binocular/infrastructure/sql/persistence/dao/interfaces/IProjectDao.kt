package com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces

import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity

internal interface IProjectDao : IDao<ProjectEntity, Long> {
    fun findByName(name: String): ProjectEntity?
    fun findByIid(iid: com.inso_world.binocular.model.Project.Id): ProjectEntity?
}
