package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.persistence.dao.interfaces.IModuleDao
import com.inso_world.binocular.web.persistence.entity.arangodb.ModuleEntity
import com.inso_world.binocular.web.persistence.mapper.arangodb.ModuleMapper
import com.inso_world.binocular.web.persistence.repository.arangodb.ModuleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Repository
@Profile("nosql", "arangodb")
class ModuleDao @Autowired constructor(
  moduleRepository: ModuleRepository,
  moduleMapper: ModuleMapper
) : MappedArangoDbDao<Module, ModuleEntity, String>(moduleRepository, moduleMapper), IModuleDao
