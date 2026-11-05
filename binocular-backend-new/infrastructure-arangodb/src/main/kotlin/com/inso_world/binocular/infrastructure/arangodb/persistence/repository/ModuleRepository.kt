package com.inso_world.binocular.infrastructure.arangodb.persistence.repository

import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ModuleEntity
import org.springframework.stereotype.Repository

@Repository
interface ModuleRepository : ArangoRepository<ModuleEntity, String>
