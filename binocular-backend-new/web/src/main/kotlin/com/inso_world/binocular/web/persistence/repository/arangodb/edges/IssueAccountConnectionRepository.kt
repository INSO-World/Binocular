package com.inso_world.binocular.web.persistence.repository.arangodb.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.edge.IssueAccountConnection
import org.springframework.stereotype.Repository

@Repository
interface IssueAccountConnectionRepository : ArangoRepository<IssueAccountConnection, String> {

  @Query("""
    FOR c IN `issues-accounts`
        FILTER c._from == CONCAT('issues/', @issueId)
        FOR a IN accounts
            FILTER a._id == c._to
            RETURN a
""")
  fun findAccountsByIssue(issueId: String): List<Account>

  @Query("""
    FOR c IN `issues-accounts`
        FILTER c._to == CONCAT('accounts/', @accountId)
        FOR i IN issues
            FILTER i._id == c._from
            RETURN i
""")
  fun findIssuesByAccount(accountId: String): List<Issue>
}
