package com.inso_world.binocular.web.persistence.repository.arangodb.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.entity.edge.IssueUserConnection
import org.springframework.stereotype.Repository

@Repository
interface IssueUserConnectionRepository: ArangoRepository<IssueUserConnection, String> {

  @Query("""
    FOR c IN `issues-users`
        FILTER c._from == CONCAT('issues/', @issueId)
        FOR u IN users
            FILTER u._id == c._to
            RETURN u
""")
  fun findUsersByIssue(issueId: String): List<User>

  @Query("""
    FOR c IN `issues-users`
        FILTER c._to == CONCAT('users/', @userId)
        FOR i IN issues
            FILTER i._id == c._from
            RETURN i
""")
  fun findIssuesByUser(userId: String): List<Issue>
}
