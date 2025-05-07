package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.Build
import com.inso_world.binocular.web.persistence.dao.interfaces.IBuildDao
import com.inso_world.binocular.web.persistence.repository.arangodb.BuildRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class BuildDao(
  @Autowired private val buildRepository: BuildRepository
) : ArangoDbDao<Build, String>(), IBuildDao {

  init {
    this.setClazz(Build::class.java)
    this.setRepository(buildRepository)
  }
}
