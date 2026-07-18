package com.inso_world.binocular.infrastructure.arangodb.persistence.repository

import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CiRatePerModuleEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ModuleAuthorCountEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ModuleEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ModuleSizeCountEntity
import org.springframework.stereotype.Repository

@Repository
interface ModuleRepository : ArangoRepository<ModuleEntity, String> {
    @com.arangodb.springframework.annotation.Query(
        """
    FOR m IN modules
      FILTER LENGTH(@neededModules) == 0 OR m.path IN @neededModules
      FOR c IN 1..1 INBOUND m `commits-modules`
        COLLECT modulePath = m.path, authorId = c.author WITH COUNT INTO cnt
        LET gitSignature = DOCUMENT(authorId).gitSignature
        RETURN { module: modulePath, gitSignature: gitSignature, count: cnt }
    """
    )
    fun countAuthorCommitsByModule(
        @org.springframework.data.repository.query.Param("neededModules") neededModules: List<String>,
    ): List<ModuleAuthorCountEntity>

    @com.arangodb.springframework.annotation.Query(
        """
    FOR m IN modules
      FILTER LENGTH(@neededModules) == 0 OR m.path IN @neededModules
      LET statuses = (
        FOR c IN 1..1 INBOUND m `commits-modules`
          FOR b IN 1..1 OUTBOUND c `commits-builds`
            FILTER b.createdAt != null
            FILTER DATE_TIMESTAMP(b.createdAt) >= @since AND DATE_TIMESTAMP(b.createdAt) <= @until
            FILTER LOWER(b.status) IN ["failed", "success"]
            COLLECT bid = b._id, status = LOWER(b.status)
            RETURN status
      )
      LET completed = LENGTH(statuses)
      FILTER completed > 0
      RETURN {
        module: m.path,
        failed: LENGTH(statuses[* FILTER CURRENT == "failed"]),
        completed: completed
      }
    """
    )
    fun ciErrorRateByModule(
        @org.springframework.data.repository.query.Param("since") since: Long,
        @org.springframework.data.repository.query.Param("until") until: Long,
        @org.springframework.data.repository.query.Param("neededModules") neededModules: List<String>,
    ): List<CiRatePerModuleEntity>

    @com.arangodb.springframework.annotation.Query(
        """
    FOR m IN modules
      FILTER LENGTH(@neededModules) == 0 OR m.path IN @neededModules
      LET loc = SUM(
        FOR f IN 1..1 OUTBOUND m `modules-files`
          FOR c, e IN 1..1 INBOUND f `commits-files`
            RETURN (e.stats.additions != null ? e.stats.additions : 0)
                - (e.stats.deletions != null ? e.stats.deletions : 0)
      )
      LET changeFrequency = LENGTH(
        FOR c IN 1..1 INBOUND m `commits-modules`
          FILTER DATE_TIMESTAMP(c.authorDateTime) >= @since
             AND DATE_TIMESTAMP(c.authorDateTime) <= @until
          RETURN DISTINCT c._id
      )
      FILTER loc > 0 OR changeFrequency > 0
      RETURN { module: m.path, loc: loc, changeFrequency: changeFrequency }
    """
    )
    fun sizeAndChangeFrequencyByModule(
        @org.springframework.data.repository.query.Param("since") since: Long,
        @org.springframework.data.repository.query.Param("until") until: Long,
        @org.springframework.data.repository.query.Param("neededModules") neededModules: List<String>,
    ): List<ModuleSizeCountEntity>

}
