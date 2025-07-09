package com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces

import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity

internal interface IRepositoryDao : IDao<RepositoryEntity, Long> {
    fun findByName(gitDir: String): RepositoryEntity?
}
