package com.inso_world.binocular.infrastructure.arangodb.persistence.repository

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.DeveloperEntity
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Spring Data ArangoDB repository for [DeveloperEntity] persistence.
 *
 * Provides CRUD operations against the `developers` collection.
 */
@Repository
interface DeveloperRepository : ArangoRepository<DeveloperEntity, String> {
    /**
     * Finds all developers belonging to the given repository key.
     *
     * ArangoDB `@Ref` fields store the full document handle (`{collection}/{_key}`),
     * so the filter concatenates the `repositories/` prefix with [repositoryKey].
     *
     * @param repositoryKey the `_key` of the owning [com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity]
     * @return all [DeveloperEntity] instances whose `repository` ref points to that key
     */
    @Query("FOR d IN developers FILTER d.repository == CONCAT('repositories/', @repositoryKey) RETURN d")
    fun findByRepositoryKey(
        @Param("repositoryKey") repositoryKey: String,
    ): List<DeveloperEntity>
}
