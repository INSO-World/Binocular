package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IMilestoneDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.MilestoneEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.MilestoneMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.MilestoneRepository
import com.inso_world.binocular.model.Milestone
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
internal class MilestoneDao
    @Autowired
    constructor(
        milestoneRepository: MilestoneRepository,
        milestoneMapper: MilestoneMapper,
    ) : MappedArangoDbDao<Milestone, MilestoneEntity, String>(milestoneRepository, milestoneMapper),
        IMilestoneDao
