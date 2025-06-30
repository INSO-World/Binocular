package com.inso_world.binocular.web.service.impl

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.Milestone
import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestAccountConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestMilestoneConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestNoteConnectionDao
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.MergeRequestDao
import com.inso_world.binocular.web.persistence.model.Page
import com.inso_world.binocular.web.service.MergeRequestService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

/**
 * Implementation of the MergeRequestService interface.
 * This service is database-agnostic and works with both ArangoDB and SQL implementations.
 */
@Service
class MergeRequestServiceImpl(
  @Autowired private val mergeRequestDao: IMergeRequestDao,
  @Autowired private val mergeRequestAccountConnectionRepository: IMergeRequestAccountConnectionDao,
  @Autowired private val mergeRequestMilestoneConnectionRepository: IMergeRequestMilestoneConnectionDao,
  @Autowired private val mergeRequestNoteConnectionRepository: IMergeRequestNoteConnectionDao
) : MergeRequestService {

  var logger: Logger = LoggerFactory.getLogger(MergeRequestServiceImpl::class.java)

  override fun findAll(pageable: Pageable): Page<MergeRequest> {
    logger.trace("Getting all merge requests with pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}")
    return mergeRequestDao.findAll(pageable)
  }

  override fun findById(id: String): MergeRequest? {
    logger.trace("Getting merge request by id: $id")
    return mergeRequestDao.findById(id)
  }

  override fun findAccountsByMergeRequestId(mergeRequestId: String): List<Account> {
    logger.trace("Getting accounts for merge request: $mergeRequestId")
    return mergeRequestAccountConnectionRepository.findAccountsByMergeRequest(mergeRequestId)
  }

  override fun findMilestonesByMergeRequestId(mergeRequestId: String): List<Milestone> {
    logger.trace("Getting milestones for merge request: $mergeRequestId")
    return mergeRequestMilestoneConnectionRepository.findMilestonesByMergeRequest(mergeRequestId)
  }

  override fun findNotesByMergeRequestId(mergeRequestId: String): List<Note> {
    logger.trace("Getting notes for merge request: $mergeRequestId")
    return mergeRequestNoteConnectionRepository.findNotesByMergeRequest(mergeRequestId)
  }
}
