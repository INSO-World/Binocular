package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.Milestone
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.MilestoneDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class MilestoneService(
  @Autowired private val milestoneDao: MilestoneDao,
) {

  var logger: Logger = LoggerFactory.getLogger(MilestoneService::class.java)

  fun findAll(page: Int? = 1, perPage: Int? = 100): Iterable<Milestone> {
    logger.trace("Getting all milestones...")
    val page = page ?: 1
    val perPage = perPage ?: 100
    logger.debug("page is $page, perPage is $perPage")
    val pageable: Pageable = PageRequest.of(page, perPage)

    return milestoneDao.findAll()
  }

  fun findById(id: String): Milestone? {
    logger.trace("Getting milestone by id: $id")
    return milestoneDao.findById(id)
  }
}
