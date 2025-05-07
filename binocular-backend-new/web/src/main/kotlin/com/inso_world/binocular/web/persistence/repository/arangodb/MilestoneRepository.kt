package com.inso_world.binocular.web.persistence.repository.arangodb

import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.entity.Milestone
import org.springframework.stereotype.Repository

@Repository
interface MilestoneRepository: ArangoRepository<Milestone, String> {
}
