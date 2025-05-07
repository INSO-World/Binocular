package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.Build
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.BuildDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class BuildService(
  @Autowired private val buildDao: BuildDao,
) {

  var logger: Logger = LoggerFactory.getLogger(BuildService::class.java)

  fun findAll(page: Int? = 1, perPage: Int? = 100): Iterable<Build> {
    logger.trace("Getting all builds...")
    val page = page ?: 1
    val perPage = perPage ?: 100
    logger.debug("page is $page, perPage is $perPage")
    val pageable: Pageable = PageRequest.of(page - 1, perPage)

    return buildDao.findAll(pageable)
  }

  fun findById(id: String): Build? {
    logger.trace("Getting build by id: $id")
    return buildDao.findById(id)
  }
}
