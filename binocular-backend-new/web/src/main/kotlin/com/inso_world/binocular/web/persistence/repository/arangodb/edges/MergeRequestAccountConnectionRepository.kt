package com.inso_world.binocular.web.persistence.repository.arangodb.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.persistence.entity.arangodb.AccountEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.MergeRequestAccountConnectionEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.MergeRequestEntity
import org.springframework.stereotype.Repository

@Repository
interface MergeRequestAccountConnectionRepository: ArangoRepository<MergeRequestAccountConnectionEntity, String> {

  @Query("""
    FOR c IN `merge-requests-accounts`
        FILTER c._from == CONCAT('mergeRequests/', @mergeRequestId)
        FOR a IN accounts
            FILTER a._id == c._to
            RETURN a
""")
  fun findAccountsByMergeRequest(mergeRequestId: String): List<AccountEntity>

  @Query("""
    FOR c IN `merge-requests-accounts`
        FILTER c._to == CONCAT('accounts/', @accountId)
        FOR mr IN mergeRequests
            FILTER mr._id == c._from
            RETURN mr
""")
  fun findMergeRequestsByAccount(accountId: String): List<MergeRequestEntity>
}
