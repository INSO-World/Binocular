package com.inso_world.binocular.infrastructure.sql.persistence.repository

import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import org.springframework.data.jpa.repository.JpaRepository

internal interface RepositoryRepository : JpaRepository<RepositoryEntity, Long> {
    fun findByName(name: String): RepositoryEntity?
}
