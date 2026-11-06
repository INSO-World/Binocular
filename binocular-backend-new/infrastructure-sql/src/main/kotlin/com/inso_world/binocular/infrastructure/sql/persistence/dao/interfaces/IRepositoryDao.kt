package com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces

import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import jakarta.validation.constraints.Size

internal interface IRepositoryDao : IDao<RepositoryEntity, Long> {
    fun findByIid(iid: com.inso_world.binocular.model.Repository.Id): RepositoryEntity?
    fun findByName(name: String): RepositoryEntity?

    fun findByIdWithAllRelations(id: Long): RepositoryEntity?
}
