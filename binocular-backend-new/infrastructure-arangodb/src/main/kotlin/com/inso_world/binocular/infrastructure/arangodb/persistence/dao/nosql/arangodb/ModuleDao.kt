package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IModuleDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ModuleEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.ModuleMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.ModuleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
internal class ModuleDao
    @Autowired
    constructor(
        moduleRepository: ModuleRepository,
        moduleMapper: ModuleMapper,
    ) : MappedArangoDbDao<com.inso_world.binocular.model.Module, ModuleEntity, String>(moduleRepository, moduleMapper),
        IModuleDao
