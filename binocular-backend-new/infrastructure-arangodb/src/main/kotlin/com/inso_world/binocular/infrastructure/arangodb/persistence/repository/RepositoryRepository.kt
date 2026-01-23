package com.inso_world.binocular.infrastructure.arangodb.persistence.repository

import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import org.springframework.stereotype.Repository

@Repository
interface RepositoryRepository : ArangoRepository<RepositoryEntity, String> {
    fun findByLocalPath(localPath: String): RepositoryEntity?
}
