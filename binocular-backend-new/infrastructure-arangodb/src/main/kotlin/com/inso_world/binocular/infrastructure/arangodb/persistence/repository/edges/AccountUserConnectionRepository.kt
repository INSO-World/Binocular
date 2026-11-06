package com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.AccountEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.UserEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.AccountUserConnectionEntity
import org.springframework.stereotype.Repository

@Repository
interface AccountUserConnectionRepository : ArangoRepository<AccountUserConnectionEntity, String> {
    @Query(
        """
    FOR c IN `accounts-users`
        FILTER c._from == CONCAT('accounts/', @accountId)
        FOR u IN users
            FILTER u._id == c._to
            RETURN u
""",
    )
    fun findUsersByAccount(accountId: String): List<UserEntity>

    @Query(
        """
    FOR c IN `accounts-users`
        FILTER c._to == CONCAT('users/', @userId)
        FOR a IN accounts
            FILTER a._id == c._from
            RETURN a
""",
    )
    fun findAccountsByUser(userId: String): List<AccountEntity>
}
