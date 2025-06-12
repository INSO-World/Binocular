package com.inso_world.binocular.web.service.impl

import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.BranchDao
import com.inso_world.binocular.web.persistence.model.Page
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.BranchFileConnectionRepository
import com.inso_world.binocular.web.service.BranchService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class BranchServiceImpl(
  @Autowired private val branchDao: BranchDao,
  @Autowired private val branchFileConnectionRepository: BranchFileConnectionRepository
) : BranchService {

  var logger: Logger = LoggerFactory.getLogger(BranchServiceImpl::class.java)

  override fun findAll(pageable: Pageable): Page<Branch> {
    logger.trace("Getting all branches with pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}")
    return branchDao.findAll(pageable)
  }

  override fun findById(id: String): Branch? {
    logger.trace("Getting branch by id: $id")
    return branchDao.findById(id)
  }

  override fun findFilesByBranchId(branchId: String): List<File> {
    logger.trace("Getting files for branch: $branchId")
    return branchFileConnectionRepository.findFilesByBranch(branchId)
  }
}
