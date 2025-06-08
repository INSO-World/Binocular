package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.Milestone
import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.MergeRequestDao
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.MergeRequestAccountConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.MergeRequestMilestoneConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.MergeRequestNoteConnectionRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class MergeRequestService(
  @Autowired private val mergeRequestDao: MergeRequestDao,
  @Autowired private val mergeRequestAccountConnectionRepository: MergeRequestAccountConnectionRepository,
  @Autowired private val mergeRequestMilestoneConnectionRepository: MergeRequestMilestoneConnectionRepository,
  @Autowired private val mergeRequestNoteConnectionRepository: MergeRequestNoteConnectionRepository
) {

  var logger: Logger = LoggerFactory.getLogger(MergeRequestService::class.java)

  fun findAll(pageable: Pageable): Iterable<MergeRequest> {
    logger.trace("Getting all merge requests with pageable: page=${pageable.pageNumber + 1}, size=${pageable.pageSize}")
    return mergeRequestDao.findAll(pageable)
  }

  fun findById(id: String): MergeRequest? {
    logger.trace("Getting merge request by id: $id")
    return mergeRequestDao.findById(id)
  }

  fun findAccountsByMergeRequestId(mergeRequestId: String): List<Account> {
    logger.trace("Getting accounts for merge request: $mergeRequestId")
    return mergeRequestAccountConnectionRepository.findAccountsByMergeRequest(mergeRequestId)
  }

  fun findMilestonesByMergeRequestId(mergeRequestId: String): List<Milestone> {
    logger.trace("Getting milestones for merge request: $mergeRequestId")
    return mergeRequestMilestoneConnectionRepository.findMilestonesByMergeRequest(mergeRequestId)
  }

  fun findNotesByMergeRequestId(mergeRequestId: String): List<Note> {
    logger.trace("Getting notes for merge request: $mergeRequestId")
    return mergeRequestNoteConnectionRepository.findNotesByMergeRequest(mergeRequestId)
  }
}
