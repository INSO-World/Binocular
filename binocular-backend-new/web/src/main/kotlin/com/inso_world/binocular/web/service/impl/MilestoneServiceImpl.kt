package com.inso_world.binocular.web.service.impl

import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Milestone
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.MilestoneDao
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueMilestoneConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.MergeRequestMilestoneConnectionRepository
import com.inso_world.binocular.web.service.MilestoneService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class MilestoneServiceImpl(
  @Autowired private val milestoneDao: MilestoneDao,
  @Autowired private val issueMilestoneConnectionRepository: IssueMilestoneConnectionRepository,
  @Autowired private val mergeRequestMilestoneConnectionRepository: MergeRequestMilestoneConnectionRepository
) : MilestoneService {

  var logger: Logger = LoggerFactory.getLogger(MilestoneServiceImpl::class.java)

  override fun findAll(pageable: Pageable): Iterable<Milestone> {
    logger.trace("Getting all milestones with pageable: page=${pageable.pageNumber + 1}, size=${pageable.pageSize}")
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
