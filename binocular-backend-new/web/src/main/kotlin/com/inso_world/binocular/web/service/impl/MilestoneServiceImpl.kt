package com.inso_world.binocular.web.service.impl

import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Milestone
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueMilestoneConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestMilestoneConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMilestoneDao
import com.inso_world.binocular.web.persistence.model.Page
import com.inso_world.binocular.web.service.MilestoneService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

/**
 * Implementation of the MilestoneService interface.
 * This service is database-agnostic and works with both ArangoDB and SQL implementations.
 */
@Service
class MilestoneServiceImpl(
  @Autowired private val milestoneDao: IMilestoneDao,
  @Autowired private val issueMilestoneConnectionRepository: IIssueMilestoneConnectionDao,
  @Autowired private val mergeRequestMilestoneConnectionRepository: IMergeRequestMilestoneConnectionDao
) : MilestoneService {

  var logger: Logger = LoggerFactory.getLogger(MilestoneServiceImpl::class.java)

  override fun findAll(pageable: Pageable): Page<Milestone> {
    logger.trace("Getting all milestones with pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}")
    return milestoneDao.findAll(pageable)
  }

  override fun findById(id: String): Milestone? {
    logger.trace("Getting milestone by id: $id")
    return milestoneDao.findById(id)
  }

  override fun findIssuesByMilestoneId(milestoneId: String): List<Issue> {
    logger.trace("Getting issues for milestone: $milestoneId")
    return issueMilestoneConnectionRepository.findIssuesByMilestone(milestoneId)
  }

  override fun findMergeRequestsByMilestoneId(milestoneId: String): List<MergeRequest> {
    logger.trace("Getting merge requests for milestone: $milestoneId")
    return mergeRequestMilestoneConnectionRepository.findMergeRequestsByMilestone(milestoneId)
  }
}
