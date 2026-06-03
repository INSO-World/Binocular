package com.inso_world.binocular.infrastructure.arangodb.persistence.repository

import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.DeveloperEntity
import org.springframework.stereotype.Repository

/**
 * Spring Data ArangoDB repository for [DeveloperEntity] persistence.
 *
 * Provides CRUD operations against the `developers` collection.
 */
@Repository
interface DeveloperRepository : ArangoRepository<DeveloperEntity, String>
