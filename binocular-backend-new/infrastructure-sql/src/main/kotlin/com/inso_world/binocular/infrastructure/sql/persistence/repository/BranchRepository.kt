package com.inso_world.binocular.infrastructure.sql.persistence.repository

import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import org.springframework.data.jpa.repository.JpaRepository

internal interface BranchRepository : JpaRepository<BranchEntity, Long>
