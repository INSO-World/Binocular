package com.inso_world.binocular.infrastructure.sql.persistence.repository

import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import org.springframework.data.jpa.repository.JpaRepository

internal interface BranchRepository : JpaRepository<BranchEntity, Long> {
    fun findByRepository_IdAndName(
        repositoryId: Long,
        name: String,
    ): BranchEntity?

    fun findAllByRepository(repo: RepositoryEntity): Collection<BranchEntity>
}
