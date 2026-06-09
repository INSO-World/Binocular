package com.inso_world.binocular.infrastructure.sql.persistence.repository

import com.inso_world.binocular.infrastructure.sql.persistence.entity.IssueEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.DeveloperEntity
import com.inso_world.binocular.model.Issue
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.util.stream.Stream

@Repository
internal interface IssueRepository : JpaRepository<IssueEntity, Long>, JpaSpecificationExecutor<IssueEntity> {
    fun findByIid(iid: Issue.Id): IssueEntity?

    fun findAllByIidIn(iids: Collection<Issue.Id>): List<IssueEntity>

    fun findAllByDevelopersContaining(user: DeveloperEntity): Stream<IssueEntity>
}
