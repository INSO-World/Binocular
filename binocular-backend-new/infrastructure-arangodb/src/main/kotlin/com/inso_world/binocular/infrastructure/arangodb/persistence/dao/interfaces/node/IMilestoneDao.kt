package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.IDao
import com.inso_world.binocular.model.Milestone

internal interface IMilestoneDao : IDao<Milestone, String>
