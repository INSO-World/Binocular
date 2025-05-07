package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.persistence.repository.arangodb.CommitRepository
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.CommitDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class CommitService(
  @Autowired private val commitDao: CommitDao,
) {

  var logger: Logger = LoggerFactory.getLogger(CommitService::class.java)


  fun findAll(page: Int? = 1, perPage: Int? = 100): Iterable<Commit> {
    logger.trace("Getting all commits...")
    val page = page ?: 1
    val perPage = perPage ?: 100
    logger.debug("page is $page, perPage is $perPage")
    val pageable: Pageable = PageRequest.of(page, perPage)

    return commitDao.findAll()
  }

  fun findById(id: String): Commit? {
    logger.trace("Getting commit by id: $id")
    return commitDao.findById(id)
  }

}
