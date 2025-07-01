package com.inso_world.binocular.web.service.impl

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueAccountConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueCommitConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueMilestoneConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueNoteConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueUserConnectionDao
import com.inso_world.binocular.web.persistence.model.Page
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueAccountConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueCommitConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueMilestoneConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueNoteConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueUserConnectionRepository
import com.inso_world.binocular.web.service.IssueService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class IssueServiceImpl(
  @Autowired private val issueDao: IIssueDao,
  @Autowired private val issueAccountConnectionRepository: IIssueAccountConnectionDao,
  @Autowired private val issueCommitConnectionRepository: IIssueCommitConnectionDao,
  @Autowired private val issueMilestoneConnectionRepository: IIssueMilestoneConnectionDao,
  @Autowired private val issueNoteConnectionRepository: IIssueNoteConnectionDao,
  @Autowired private val issueUserConnectionRepository: IIssueUserConnectionDao
) : IssueService {

  var logger: Logger = LoggerFactory.getLogger(IssueServiceImpl::class.java)

  override fun findAll(pageable: Pageable): Page<Issue> {
    logger.trace("Getting all issues with pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}")
    return issueDao.findAll(pageable)
  }

  override fun findById(id: String): Issue? {
    logger.trace("Getting issue by id: $id")
    return issueDao.findById(id)
  }

  override fun findAccountsByIssueId(issueId: String): List<Account> {
    logger.trace("Getting accounts for issue: $issueId")
    return issueAccountConnectionRepository.findAccountsByIssue(issueId)
  }

  override fun findCommitsByIssueId(issueId: String): List<Commit> {
    logger.trace("Getting commits for issue: $issueId")
    return issueCommitConnectionRepository.findCommitsByIssue(issueId)
  }

  override fun findMilestonesByIssueId(issueId: String): List<Milestone> {
    logger.trace("Getting milestones for issue: $issueId")
    return issueMilestoneConnectionRepository.findMilestonesByIssue(issueId)
  }

  override fun findNotesByIssueId(issueId: String): List<Note> {
    logger.trace("Getting notes for issue: $issueId")
    return issueNoteConnectionRepository.findNotesByIssue(issueId)
  }

  override fun findUsersByIssueId(issueId: String): List<User> {
    logger.trace("Getting users for issue: $issueId")
    return issueUserConnectionRepository.findUsersByIssue(issueId)
  }
}
