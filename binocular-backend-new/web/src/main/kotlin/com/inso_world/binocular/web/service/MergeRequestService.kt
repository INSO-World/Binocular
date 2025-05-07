package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.MergeRequestDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class MergeRequestService(
  @Autowired private val mergeRequestDao: MergeRequestDao,
) {

  var logger: Logger = LoggerFactory.getLogger(MergeRequestService::class.java)

  fun findAll(page: Int? = 1, perPage: Int? = 100): Iterable<MergeRequest> {
    logger.trace("Getting all merge requests...")
    val page = page ?: 1
    val perPage = perPage ?: 100
    logger.debug("page is $page, perPage is $perPage")
    val pageable: Pageable = PageRequest.of(page, perPage)

    return mergeRequestDao.findAll()
  }

  fun findById(id: String): MergeRequest? {
    logger.trace("Getting merge request by id: $id")
    return mergeRequestDao.findById(id)
  }
}
