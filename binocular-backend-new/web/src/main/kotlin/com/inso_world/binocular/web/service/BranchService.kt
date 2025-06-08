package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.BranchDao
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.BranchFileConnectionRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class BranchService(
  @Autowired private val branchDao: BranchDao,
  @Autowired private val branchFileConnectionRepository: BranchFileConnectionRepository
) {

  var logger: Logger = LoggerFactory.getLogger(BranchService::class.java)

  fun findAll(pageable: Pageable): Iterable<Branch> {
    logger.trace("Getting all branches with pageable: page=${pageable.pageNumber + 1}, size=${pageable.pageSize}")
    return branchDao.findAll(pageable)
  }

  fun findById(id: String): Branch? {
    logger.trace("Getting branch by id: $id")
    return branchDao.findById(id)
  }

  fun findFilesByBranchId(branchId: String): List<File> {
    logger.trace("Getting files for branch: $branchId")
    return branchFileConnectionRepository.findFilesByBranch(branchId)
  }
}
