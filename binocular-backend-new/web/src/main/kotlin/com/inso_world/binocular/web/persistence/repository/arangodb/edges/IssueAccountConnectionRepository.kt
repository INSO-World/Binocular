package com.inso_world.binocular.web.persistence.repository.arangodb.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.persistence.entity.arangodb.AccountEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.IssueAccountConnectionEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.IssueEntity
import org.springframework.stereotype.Repository

@Repository
interface IssueAccountConnectionRepository : ArangoRepository<IssueAccountConnectionEntity, String> {

  @Query("""
    FOR c IN `issues-accounts`
        FILTER c._from == CONCAT('issues/', @issueId)
        FOR a IN accounts
            FILTER a._id == c._to
            RETURN a
""")
  fun findAccountsByIssue(issueId: String): List<AccountEntity>

  @Query("""
    FOR c IN `issues-accounts`
        FILTER c._to == CONCAT('accounts/', @accountId)
        FOR i IN issues
            FILTER i._id == c._from
            RETURN i
""")
  fun findIssuesByAccount(accountId: String): List<IssueEntity>
}
