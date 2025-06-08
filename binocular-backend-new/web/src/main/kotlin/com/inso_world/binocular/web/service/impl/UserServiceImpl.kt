package com.inso_world.binocular.web.service.impl

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.UserDao
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitUserConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitFileUserConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueUserConnectionRepository
import com.inso_world.binocular.web.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
  @Autowired private val userDao: UserDao,
  @Autowired private val commitUserConnectionRepository: CommitUserConnectionRepository,
  @Autowired private val commitFileUserConnectionRepository: CommitFileUserConnectionRepository,
  @Autowired private val issueUserConnectionRepository: IssueUserConnectionRepository
) : UserService {

  var logger: Logger = LoggerFactory.getLogger(UserServiceImpl::class.java)

  override fun findAll(pageable: Pageable): Iterable<User> {
    logger.trace("Getting all users with pageable: page=${pageable.pageNumber + 1}, size=${pageable.pageSize}")
    return userDao.findAll(pageable)
  }

  override fun findById(id: String): User? {
    logger.trace("Getting user by id: $id")
    return userDao.findById(id)
  }

  override fun findCommitsByUserId(userId: String): List<Commit> {
    logger.trace("Getting commits for user: $userId")
    return commitUserConnectionRepository.findCommitsByUser(userId)
  }

  override fun findIssuesByUserId(userId: String): List<Issue> {
    logger.trace("Getting issues for user: $userId")
    return issueUserConnectionRepository.findIssuesByUser(userId)
  }

  override fun findFilesByUserId(userId: String): List<File> {
    logger.trace("Getting files for user: $userId")
    return commitFileUserConnectionRepository.findCommitFilesByUser(userId)
  }
}
