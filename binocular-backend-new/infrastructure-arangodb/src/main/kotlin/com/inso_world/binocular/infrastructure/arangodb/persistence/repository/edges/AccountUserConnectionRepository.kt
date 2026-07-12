package com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.AccountEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.UserEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.AccountUserConnectionEntity
import org.springframework.data.repository.query.Param
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

    @Query(
        """
    INSERT {
      _from: @from,
      _to: @to,
      matchLayer: @layer,
      confidence: @confidence,
      status: @status,
      signals: @signals,
      reviewedBy: null,
      resolvedAt: null
    } INTO `accounts-users`
    RETURN NEW
    """
    )
    fun insertMatch(
        @Param("from") from: String,
        @Param("to") to: String,
        @Param("layer") layer: String,
        @Param("confidence") confidence: Double,
        @Param("status") status: String,
        @Param("signals") signals: String,
    ): Map<String, Any>

    @Query("FOR c IN `accounts-users` REMOVE c IN `accounts-users`")
    fun clearAll()

    @Query("FOR a IN accounts RETURN { id: a._id, login: a.login, name: a.name, email: a.email }")
    fun findAllAccountsFlat(): List<Map<String, Any?>>

    @Query("FOR u IN users RETURN { id: u._id, gitSignature: u.gitSignature }")
    fun findAllUsersFlat(): List<Map<String, Any?>>

    @Query(
        """
FOR c IN `accounts-users`
  FILTER c.matchLayer != null
  LET acc = DOCUMENT(c._from)
  LET usr = DOCUMENT(c._to)
  RETURN {
    matchId: c._key,
    accountLogin: acc.login,
    accountName: acc.name,
    userGitSignature: usr.gitSignature,
    matchLayer: c.matchLayer,
    confidence: c.confidence,
    status: c.status,
    signals: c.signals,
    reviewedBy: c.reviewedBy,
    resolvedAt: c.resolvedAt
  }
"""
    )
    fun findAllMatches(): List<Map<String, Any?>>

    @Query(
        """
FOR c IN `accounts-users`
  FILTER c.status == "PENDING_REVIEW" || c.status == "LOW_CONFIDENCE"
  LET acc = DOCUMENT(c._from)
  LET usr = DOCUMENT(c._to)
  RETURN {
    matchId: c._key,
    accountLogin: acc.login,
    accountName: acc.name,
    userGitSignature: usr.gitSignature,
    matchLayer: c.matchLayer,
    confidence: c.confidence,
    status: c.status,
    signals: c.signals,
    reviewedBy: c.reviewedBy,
    resolvedAt: c.resolvedAt
  }
"""
    )
    fun findPendingMatches(): List<Map<String, Any?>>

    @Query(
        """
FOR c IN `accounts-users`
  FILTER c._key == @matchKey
  RETURN { status: c.status, confidence: c.confidence }
"""
    )
    fun findMatchStatusAndConfidence(@Param("matchKey") matchKey: String): Map<String, Any?>?

    @Query(
        """
    FOR c IN `accounts-users`
        FILTER c._key == @matchKey
        UPDATE c WITH {
            status: @status,
            reviewedBy: @reviewedBy,
            resolvedAt: @resolvedAt
        } IN `accounts-users`
        RETURN NEW
    """
    )
    fun reviewMatch(
        @Param("matchKey") matchKey: String,
        @Param("status") status: String,
        @Param("reviewedBy") reviewedBy: String,
        @Param("resolvedAt") resolvedAt: String,
    ): Map<String, Any>?

    @Query(
        """
    FOR c IN `accounts-users`
        FILTER c._key == @matchKey
        UPDATE c WITH {
            status: @status,
            reviewedBy: null,
            resolvedAt: null
        } IN `accounts-users`
        RETURN NEW
    """
    )
    fun undoReview(
        @Param("matchKey") matchKey: String,
        @Param("status") status: String,
    ): Map<String, Any>?
}
