package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.IssueDao
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueAccountConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueCommitConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueMilestoneConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueNoteConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueUserConnectionRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class IssueService(
  @Autowired private val issueDao: IssueDao,
  @Autowired private val issueAccountConnectionRepository: IssueAccountConnectionRepository,
  @Autowired private val issueCommitConnectionRepository: IssueCommitConnectionRepository,
  @Autowired private val issueMilestoneConnectionRepository: IssueMilestoneConnectionRepository,
  @Autowired private val issueNoteConnectionRepository: IssueNoteConnectionRepository,
  @Autowired private val issueUserConnectionRepository: IssueUserConnectionRepository
) {

  var logger: Logger = LoggerFactory.getLogger(IssueService::class.java)

  fun findAll(pageable: Pageable): Iterable<Issue> {
    logger.trace("Getting all issues with pageable: page=${pageable.pageNumber + 1}, size=${pageable.pageSize}")
    return issueDao.findAll(pageable)
  }

  fun findById(id: String): Issue? {
    logger.trace("Getting issue by id: $id")
    return issueDao.findById(id)
  }

  fun findAccountsByIssueId(issueId: String): List<Account> {
    logger.trace("Getting accounts for issue: $issueId")
    return issueAccountConnectionRepository.findAccountsByIssue(issueId)
  }

  fun findCommitsByIssueId(issueId: String): List<Commit> {
    logger.trace("Getting commits for issue: $issueId")
    return issueCommitConnectionRepository.findCommitsByIssue(issueId)
  }

  fun findMilestonesByIssueId(issueId: String): List<Milestone> {
    logger.trace("Getting milestones for issue: $issueId")
    return issueMilestoneConnectionRepository.findMilestonesByIssue(issueId)
  }

  fun findNotesByIssueId(issueId: String): List<Note> {
    logger.trace("Getting notes for issue: $issueId")
    return issueNoteConnectionRepository.findNotesByIssue(issueId)
  }

  fun findUsersByIssueId(issueId: String): List<User> {
    logger.trace("Getting users for issue: $issueId")
    return issueUserConnectionRepository.findUsersByIssue(issueId)
  }
}
