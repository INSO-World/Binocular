package com.inso_world.binocular.infrastructure.sql.persistence.repository

import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

internal interface RepositoryRepository : JpaRepository<RepositoryEntity, Long>, JpaSpecificationExecutor<RepositoryEntity> {
    fun findByName(name: String): RepositoryEntity?
}
