package com.inso_world.binocular.infrastructure.sql.persistence.repository

import com.inso_world.binocular.infrastructure.sql.persistence.entity.IssueEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.util.stream.Stream

@Repository
internal interface IssueRepository : JpaRepository<IssueEntity, Long>, JpaSpecificationExecutor<IssueEntity> {
    fun findAllByUsersContaining(user: UserEntity): Stream<IssueEntity>
}
