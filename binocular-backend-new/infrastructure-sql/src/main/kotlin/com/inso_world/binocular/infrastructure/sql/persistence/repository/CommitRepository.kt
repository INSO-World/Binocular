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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
            SELECT b.head FROM BranchEntity b WHERE (b.name = :branch) AND (b.repository.id = :repoId)
 """,
    )
    fun findLeafCommitsByRepository(
        @Param("repoId") repoId: Long,
        @Param("branch") branch: String,
    ): CommitEntity?

    @Query(
        """
            SELECT b.head FROM BranchEntity b WHERE (b.repository.id = :repoId)
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

    @OptIn(ExperimentalUuidApi::class)
    fun findByIid(iid: Uuid): CommitEntity?

    @Query("""
            select ch from CommitEntity c 
            join c.children ch 
            where c.sha = :sha
        """)
        fun findChildrenBySha(@Param("sha") sha: String): Set<CommitEntity>

    @Query(
        """
                select p from CommitEntity c 
                join c.parents p 
                where c.sha = :sha
            """,
    )
    fun findParentsBySha(@Param("sha") sha: String): Set<CommitEntity>
}
