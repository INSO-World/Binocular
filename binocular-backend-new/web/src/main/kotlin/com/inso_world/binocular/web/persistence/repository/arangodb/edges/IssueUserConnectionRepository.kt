package com.inso_world.binocular.web.persistence.repository.arangodb.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.persistence.entity.arangodb.IssueEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.IssueUserConnectionEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.UserEntity
import org.springframework.stereotype.Repository

@Repository
interface IssueUserConnectionRepository: ArangoRepository<IssueUserConnectionEntity, String> {

  @Query("""
    FOR c IN `issues-users`
        FILTER c._from == CONCAT('issues/', @issueId)
        FOR u IN users
            FILTER u._id == c._to
            RETURN u
""")
  fun findUsersByIssue(issueId: String): List<UserEntity>

  @Query("""
    FOR c IN `issues-users`
        FILTER c._to == CONCAT('users/', @userId)
        FOR i IN issues
            FILTER i._id == c._from
            RETURN i
""")
  fun findIssuesByUser(userId: String): List<IssueEntity>
}
