package com.inso_world.binocular.infrastructure.sql.persistence.repository

import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import jakarta.persistence.QueryHint
import org.hibernate.jpa.AvailableHints
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.stereotype.Repository
import java.util.stream.Stream

@Repository
internal interface UserRepository : JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {
    fun findAllByEmailIn(emails: Collection<String>): Stream<UserEntity>
    fun findAllByRepository_Id(id: Long): Stream<UserEntity>
}
