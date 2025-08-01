package com.inso_world.binocular.infrastructure.arangodb.persistence.repository

import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.MilestoneEntity
import org.springframework.stereotype.Repository

@Repository
interface MilestoneRepository : ArangoRepository<MilestoneEntity, String>
