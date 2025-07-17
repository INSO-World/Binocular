package com.inso_world.binocular.infrastructure.sql.persistence.repository

import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.stream.Stream

@Repository
internal interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findAllByEmailIn(emails: Collection<String>): Stream<UserEntity>
}
