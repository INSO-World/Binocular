package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IBuildDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.BuildEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.BuildMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.BuildRepository
import com.inso_world.binocular.model.Build
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IBuildDao using the MappedArangoDbDao approach.
 *
 * This class extends MappedArangoDbDao to leverage the entity mapping pattern,
 * which provides a clean separation between domain models (Build) and
 * database-specific entities (BuildEntity).
 */

@Repository
class BuildDao(
    @Autowired buildRepository: BuildRepository,
    @Autowired buildMapper: BuildMapper,
) : MappedArangoDbDao<Build, BuildEntity, String>(buildRepository, buildMapper),
    IBuildDao
