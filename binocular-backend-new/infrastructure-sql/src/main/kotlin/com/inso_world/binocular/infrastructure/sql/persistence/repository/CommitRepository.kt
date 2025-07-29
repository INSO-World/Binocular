package com.inso_world.binocular.infrastructure.sql.persistence.repository

import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.stream.Stream

@Repository
internal interface CommitRepository : JpaRepository<CommitEntity, Long>, JpaSpecificationExecutor<CommitEntity> {
    /**
     * Fetches only the SHAs that actually exist in the database.
     */
    @Suppress("ktlint:standard:function-naming")
    fun findAllByRepository_IdAndShaIn(
        repoId: Long,
        shas: Collection<String>,
    ): Set<CommitEntity>

    @Suppress("ktlint:standard:function-naming")
    fun findAllByRepository_Id(repoId: Long): Stream<CommitEntity>

    @Query("SELECT c FROM CommitEntity c")
//    @EntityGraph("Commit.full")
    fun findAllAsStream(): Stream<CommitEntity>

    @Query(
        """
    SELECT c FROM CommitEntity c
    JOIN c.branches b
    WHERE (b.name = :branch)
        AND (c.repository.id = :repoId)
        AND c.sha NOT IN (
            SELECT p.sha FROM CommitEntity c2 JOIN c2.parents p WHERE c2.repository.id = :repoId
        )
 """,
    )
    fun findLeafCommitsByRepository(
        @Param("repoId") repoId: Long,
        @Param("branch") branch: String,
    ): CommitEntity?

    @Query(
        """
    SELECT c FROM CommitEntity c
    WHERE (c.repository.id = :repoId)
        AND c.sha NOT IN (
            SELECT p.sha FROM CommitEntity c2 JOIN c2.parents p WHERE c2.repository.id = :repoId
        )
 """,
    )
    fun findAllLeafCommits(
        @Param("repoId") repoId: Long,
    ): Iterable<CommitEntity>

    @Suppress("ktlint:standard:function-naming")
    fun findByRepository_IdAndSha(
        repoId: Long,
        sha: String,
    ): CommitEntity?
}
