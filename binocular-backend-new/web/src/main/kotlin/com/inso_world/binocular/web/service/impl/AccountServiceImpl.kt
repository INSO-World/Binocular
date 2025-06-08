package com.inso_world.binocular.web.service.impl

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.AccountDao
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueAccountConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.MergeRequestAccountConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.NoteAccountConnectionRepository
import com.inso_world.binocular.web.service.AccountService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class AccountServiceImpl(
  @Autowired private val accountDao: AccountDao,
  @Autowired private val issueAccountConnectionRepository: IssueAccountConnectionRepository,
  @Autowired private val mergeRequestAccountConnectionRepository: MergeRequestAccountConnectionRepository,
  @Autowired private val noteAccountConnectionRepository: NoteAccountConnectionRepository
) : AccountService {

  var logger: Logger = LoggerFactory.getLogger(AccountServiceImpl::class.java)

  override fun findAll(pageable: Pageable): Iterable<Account> {
    logger.trace("Getting all accounts with pageable: page=${pageable.pageNumber + 1}, size=${pageable.pageSize}")
    return accountDao.findAll(pageable)
  }

  override fun findById(id: String): Account? {
    logger.trace("Getting account by id: $id")
    return accountDao.findById(id)
  }

  override fun findIssuesByAccountId(accountId: String): List<Issue> {
    logger.trace("Getting issues for account: $accountId")
    return issueAccountConnectionRepository.findIssuesByAccount(accountId)
  }

  override fun findMergeRequestsByAccountId(accountId: String): List<MergeRequest> {
    logger.trace("Getting merge requests for account: $accountId")
    return mergeRequestAccountConnectionRepository.findMergeRequestsByAccount(accountId)
  }

  override fun findNotesByAccountId(accountId: String): List<Note> {
    logger.trace("Getting notes for account: $accountId")
    return noteAccountConnectionRepository.findNotesByAccount(accountId)
  }
}
