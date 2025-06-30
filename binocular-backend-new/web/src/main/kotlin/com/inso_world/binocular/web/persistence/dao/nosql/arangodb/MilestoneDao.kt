package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.Milestone
import com.inso_world.binocular.web.persistence.dao.interfaces.IMilestoneDao
import com.inso_world.binocular.web.persistence.entity.arangodb.MilestoneEntity
import com.inso_world.binocular.web.persistence.mapper.arangodb.MilestoneMapper
import com.inso_world.binocular.web.persistence.repository.arangodb.MilestoneRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Repository
@Profile("nosql", "arangodb")
class MilestoneDao @Autowired constructor(
  milestoneRepository: MilestoneRepository,
  milestoneMapper: MilestoneMapper
) : MappedArangoDbDao<Milestone, MilestoneEntity, String>(milestoneRepository, milestoneMapper), IMilestoneDao
