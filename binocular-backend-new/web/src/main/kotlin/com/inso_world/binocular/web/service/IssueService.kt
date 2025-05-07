package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.IssueDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class IssueService(
  @Autowired private val issueDao: IssueDao,
) {

  var logger: Logger = LoggerFactory.getLogger(IssueService::class.java)

  fun findAll(page: Int? = 1, perPage: Int? = 100): Iterable<Issue> {
    logger.trace("Getting all issues...")
    val page = page ?: 1
    val perPage = perPage ?: 100
    logger.debug("page is $page, perPage is $perPage")
    val pageable: Pageable = PageRequest.of(page, perPage)

    return issueDao.findAll()
  }

  fun findById(id: String): Issue? {
    logger.trace("Getting issue by id: $id")
    return issueDao.findById(id)
  }
}
