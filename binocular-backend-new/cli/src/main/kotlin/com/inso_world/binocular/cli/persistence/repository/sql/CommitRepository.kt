package com.inso_world.binocular.cli.persistence.repository.sql

import com.inso_world.binocular.cli.entity.Commit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CommitRepository : JpaRepository<Commit, String> {
  /**
   * Fetches only the SHAs that actually exist in the database.
   */
//  @Query("SELECT c.sha FROM Commit c WHERE c.sha IN :shas")
  fun findAllByRepository_IdAndShaIn(repoId: Long, shas: Collection<String>): Set<Commit>

  fun findAllByRepository_Id(repoId: Long): Set<Commit>

  @Query(
    """
    SELECT c FROM Commit c
    JOIN c.branches b
    WHERE (b.name = :branch)
        AND (c.repository.id = :repoId)
        AND c.sha NOT IN (
            SELECT p.sha FROM Commit c2 JOIN c2.parents p WHERE c2.repository.id = :repoId
        )
"""
  )
  fun findLeafCommitsByRepository(@Param("repoId") repoId: Long, @Param("branch") branch: String): Commit?

  @Query(
    """
    SELECT c FROM Commit c
    WHERE (c.repository.id = :repoId)
        AND c.sha NOT IN (
            SELECT p.sha FROM Commit c2 JOIN c2.parents p WHERE c2.repository.id = :repoId
        )
"""
  )
  fun findAllLeafCommits(@Param("repoId") repoId: Long): Iterable<Commit>
}
