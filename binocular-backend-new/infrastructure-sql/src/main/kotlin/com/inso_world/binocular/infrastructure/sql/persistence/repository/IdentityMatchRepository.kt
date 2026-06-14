package com.inso_world.binocular.infrastructure.sql.persistence.repository

import com.inso_world.binocular.infrastructure.sql.persistence.entity.IdentityMatchEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
internal interface IdentityMatchRepository : JpaRepository<IdentityMatchEntity, Long> {
    fun findByDeveloper_Id(developerId: Long): IdentityMatchEntity?
    fun findAllByStatus(status: String): List<IdentityMatchEntity>
}
