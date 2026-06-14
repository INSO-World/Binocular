package com.inso_world.binocular.infrastructure.sql.persistence.repository

import com.inso_world.binocular.infrastructure.sql.persistence.entity.AccountEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
internal interface AccountRepository : JpaRepository<AccountEntity, Long> {
    fun findByPlatformAndLogin(platform: String, login: String): AccountEntity?
}
