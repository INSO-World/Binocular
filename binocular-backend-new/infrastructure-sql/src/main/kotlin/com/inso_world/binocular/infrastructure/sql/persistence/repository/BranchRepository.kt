package com.inso_world.binocular.infrastructure.sql.persistence.repository

import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

internal interface BranchRepository : JpaRepository<BranchEntity, Long>, JpaSpecificationExecutor<BranchEntity> {
    fun findByRepository_IdAndName(
        repositoryId: Long,
        name: String,
    ): BranchEntity?
}
