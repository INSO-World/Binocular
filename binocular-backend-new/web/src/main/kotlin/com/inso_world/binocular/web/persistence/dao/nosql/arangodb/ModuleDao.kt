package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.persistence.dao.interfaces.IModuleDao
import com.inso_world.binocular.web.persistence.repository.arangodb.ModuleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class ModuleDao(
  @Autowired private val moduleRepository: ModuleRepository
) : ArangoDbDao<Module, String>(), IModuleDao {

  init {
    this.setClazz(Module::class.java)
    this.setRepository(moduleRepository)
  }
}
