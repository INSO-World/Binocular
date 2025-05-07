package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.BranchDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class BranchService(
  @Autowired private val branchDao: BranchDao,
) {

  var logger: Logger = LoggerFactory.getLogger(BranchService::class.java)

  fun findAll(page: Int? = 1, perPage: Int? = 100): Iterable<Branch> {
    logger.trace("Getting all branches...")
    val page = page ?: 1
    val perPage = perPage ?: 100
    logger.debug("page is $page, perPage is $perPage")
    val pageable: Pageable = PageRequest.of(page, perPage)

    return branchDao.findAll()
  }

  fun findById(id: String): Branch? {
    logger.trace("Getting branch by id: $id")
    return branchDao.findById(id)
  }
}
