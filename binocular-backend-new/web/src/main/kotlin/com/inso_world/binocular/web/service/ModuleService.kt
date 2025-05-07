package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.ModuleDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ModuleService(
  @Autowired private val moduleDao: ModuleDao,
) {

  var logger: Logger = LoggerFactory.getLogger(ModuleService::class.java)

  fun findAll(page: Int? = 1, perPage: Int? = 100): Iterable<Module> {
    logger.trace("Getting all modules...")
    val page = page ?: 1
    val perPage = perPage ?: 100
    logger.debug("page is $page, perPage is $perPage")
    val pageable: Pageable = PageRequest.of(page, perPage)

    return moduleDao.findAll()
  }

  fun findById(id: String): Module? {
    logger.trace("Getting module by id: $id")
    return moduleDao.findById(id)
  }
}
