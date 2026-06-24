package com.inso_world.binocular.infrastructure.arangodb.persistence.repository

import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.AuthorPeriodCountEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CommitEntity
import org.springframework.stereotype.Repository

@Repository
interface CommitRepository : ArangoRepository<CommitEntity, String>{
    @com.arangodb.springframework.annotation.Query(
        """
    FOR c IN commits
      FILTER DATE_TIMESTAMP(c.authorDateTime) <= @until
      LET period = DATE_TIMESTAMP(c.authorDateTime) < @start
                   ? @firstLabel
                   : DATE_FORMAT(c.authorDateTime, @fmt)
      COLLECT authorId = c.author, p = period WITH COUNT INTO cnt
      RETURN { gitSignature: DOCUMENT(authorId).gitSignature, period: p, count: cnt }
    """
    )
    fun findAuthorCommitCountsByPeriod(
        @org.springframework.data.repository.query.Param("until") until: Long,
        @org.springframework.data.repository.query.Param("start") start: Long,
        @org.springframework.data.repository.query.Param("firstLabel") firstLabel: String,
        @org.springframework.data.repository.query.Param("fmt") fmt: String,
    ): List<AuthorPeriodCountEntity>
}

