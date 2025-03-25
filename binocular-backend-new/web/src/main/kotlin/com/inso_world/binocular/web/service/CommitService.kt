package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.dao.CommitRepository
import com.inso_world.binocular.web.entity.Commit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class CommitService(
  @Autowired private val commitRepository: CommitRepository,
) {

  var logger: Logger = LoggerFactory.getLogger(CommitService::class.java)


  fun findAll(page: Int? = 1, perPage: Int? = 100): List<Commit> {
    logger.trace("Getting all commits...")
    val page = page ?: 1
    val perPage = perPage ?: 100
    logger.debug("page is $page, perPage is $perPage")
    val pageable: Pageable = PageRequest.of(page, perPage)

    return commitRepository.findAll(pageable).content
  }

}
