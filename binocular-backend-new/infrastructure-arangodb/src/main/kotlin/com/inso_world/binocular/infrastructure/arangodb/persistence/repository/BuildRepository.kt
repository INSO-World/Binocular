package com.inso_world.binocular.infrastructure.arangodb.persistence.repository

import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.BuildEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CiRateBucketEntity
import org.springframework.stereotype.Repository

@Repository
interface BuildRepository : ArangoRepository<BuildEntity, String> {
    @com.arangodb.springframework.annotation.Query(
        """
        FOR b IN builds
          FILTER b.createdAt == null || DATE_TIMESTAMP(b.createdAt) <= @until
          SORT b.createdAt ASC, b.committedAt ASC, TO_NUMBER(b._key) ASC, b._key ASC
          LIMIT @offset, @size
          RETURN b
        """
    )
    fun findAllUntilAsc(
        @org.springframework.data.repository.query.Param("offset") offset: Int,
        @org.springframework.data.repository.query.Param("size") size: Int,
        @org.springframework.data.repository.query.Param("until") until: Long
    ): List<BuildEntity>

    @com.arangodb.springframework.annotation.Query(
        """
        FOR b IN builds
          FILTER b.createdAt == null || DATE_TIMESTAMP(b.createdAt) <= @until
          SORT b.createdAt DESC, b.committedAt DESC, TO_NUMBER(b._key) DESC, b._key DESC
          LIMIT @offset, @size
          RETURN b
        """
    )
    fun findAllUntilDesc(
        @org.springframework.data.repository.query.Param("offset") offset: Int,
        @org.springframework.data.repository.query.Param("size") size: Int,
        @org.springframework.data.repository.query.Param("until") until: Long
    ): List<BuildEntity>

    @com.arangodb.springframework.annotation.Query(
        """
        RETURN LENGTH(
          FOR b IN builds
            FILTER b.createdAt == null || DATE_TIMESTAMP(b.createdAt) <= @until
            RETURN 1
        )
        """
    )
    fun countAllUntil(
        @org.springframework.data.repository.query.Param("until") until: Long
    ): Long

    @com.arangodb.springframework.annotation.Query(
        """
        FOR b IN builds
          FILTER b.createdAt == null || DATE_TIMESTAMP(b.createdAt) >= @since
          SORT b.createdAt ASC, b.committedAt ASC, TO_NUMBER(b._key) ASC, b._key ASC
          LIMIT @offset, @size
          RETURN b
        """
    )
    fun findAllSinceAsc(
        @org.springframework.data.repository.query.Param("offset") offset: Int,
        @org.springframework.data.repository.query.Param("size") size: Int,
        @org.springframework.data.repository.query.Param("since") since: Long
    ): List<BuildEntity>

    @com.arangodb.springframework.annotation.Query(
        """
        FOR b IN builds
          FILTER b.createdAt == null || DATE_TIMESTAMP(b.createdAt) >= @since
          SORT b.createdAt DESC, b.committedAt DESC, TO_NUMBER(b._key) DESC, b._key DESC
          LIMIT @offset, @size
          RETURN b
        """
    )
    fun findAllSinceDesc(
        @org.springframework.data.repository.query.Param("offset") offset: Int,
        @org.springframework.data.repository.query.Param("size") size: Int,
        @org.springframework.data.repository.query.Param("since") since: Long
    ): List<BuildEntity>

    @com.arangodb.springframework.annotation.Query(
        """
        FOR b IN builds
          FILTER b.createdAt == null || (DATE_TIMESTAMP(b.createdAt) >= @since && DATE_TIMESTAMP(b.createdAt) <= @until)
          SORT b.createdAt ASC, b.committedAt ASC, TO_NUMBER(b._key) ASC, b._key ASC
          LIMIT @offset, @size
          RETURN b
        """
    )
    fun findAllBetweenAsc(
        @org.springframework.data.repository.query.Param("offset") offset: Int,
        @org.springframework.data.repository.query.Param("size") size: Int,
        @org.springframework.data.repository.query.Param("since") since: Long,
        @org.springframework.data.repository.query.Param("until") until: Long
    ): List<BuildEntity>

    @com.arangodb.springframework.annotation.Query(
        """
        FOR b IN builds
          FILTER b.createdAt == null || (DATE_TIMESTAMP(b.createdAt) >= @since && DATE_TIMESTAMP(b.createdAt) <= @until)
          SORT b.createdAt DESC, b.committedAt DESC, TO_NUMBER(b._key) DESC, b._key DESC
          LIMIT @offset, @size
          RETURN b
        """
    )
    fun findAllBetweenDesc(
        @org.springframework.data.repository.query.Param("offset") offset: Int,
        @org.springframework.data.repository.query.Param("size") size: Int,
        @org.springframework.data.repository.query.Param("since") since: Long,
        @org.springframework.data.repository.query.Param("until") until: Long
    ): List<BuildEntity>

    @com.arangodb.springframework.annotation.Query(
        """
        RETURN LENGTH(
          FOR b IN builds
            FILTER b.createdAt == null || DATE_TIMESTAMP(b.createdAt) >= @since
            RETURN 1
        )
        """
    )
    fun countAllSince(
        @org.springframework.data.repository.query.Param("since") since: Long
    ): Long

    @com.arangodb.springframework.annotation.Query(
        """
        RETURN LENGTH(
          FOR b IN builds
            FILTER b.createdAt == null || (DATE_TIMESTAMP(b.createdAt) >= @since && DATE_TIMESTAMP(b.createdAt) <= @until)
            RETURN 1
        )
        """
    )
    fun countAllBetween(
        @org.springframework.data.repository.query.Param("since") since: Long,
        @org.springframework.data.repository.query.Param("until") until: Long
    ): Long

    @com.arangodb.springframework.annotation.Query(
        """
    FOR b IN builds
      FILTER b.createdAt != null
      FILTER DATE_TIMESTAMP(b.createdAt) >= @since AND DATE_TIMESTAMP(b.createdAt) <= @until
      FILTER LOWER(b.status) IN ["failed", "success"]
      COLLECT period = DATE_FORMAT(b.createdAt, @fmt)
      AGGREGATE failed    = SUM(LOWER(b.status) == "failed" ? 1 : 0),
                completed = SUM(1)
      RETURN { period: period, failed: failed, completed: completed }
    """
    )
    fun findCiErrorRateBuckets(
        @org.springframework.data.repository.query.Param("since") since: Long,
        @org.springframework.data.repository.query.Param("until") until: Long,
        @org.springframework.data.repository.query.Param("fmt") fmt: String,
    ): List<CiRateBucketEntity>
}
