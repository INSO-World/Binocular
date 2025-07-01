package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.Build
import com.inso_world.binocular.web.persistence.dao.interfaces.IBuildDao
import com.inso_world.binocular.web.persistence.entity.arangodb.BuildEntity
import com.inso_world.binocular.web.persistence.mapper.arangodb.BuildMapper
import com.inso_world.binocular.web.persistence.repository.arangodb.BuildRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IBuildDao using the MappedArangoDbDao approach.
 * 
 * This class extends MappedArangoDbDao to leverage the entity mapping pattern,
 * which provides a clean separation between domain models (Build) and 
 * database-specific entities (BuildEntity).
 */
@Repository
@Profile("nosql", "arangodb")
class BuildDao(
  @Autowired buildRepository: BuildRepository,
  @Autowired buildMapper: BuildMapper
) : MappedArangoDbDao<Build, BuildEntity, String>(buildRepository, buildMapper), IBuildDao {
}
