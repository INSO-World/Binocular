package com.inso_world.binocular.web.service.impl

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitFileUserConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitUserConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueUserConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IUserDao
import com.inso_world.binocular.web.persistence.model.Page
import com.inso_world.binocular.web.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

/**
 * Implementation of the UserService interface.
 * This service is database-agnostic and works with both ArangoDB and SQL implementations.
 */
@Service
class UserServiceImpl(
  @Autowired private val userDao: IUserDao,
  @Autowired private val commitUserConnectionRepository: ICommitUserConnectionDao,
  @Autowired private val commitFileUserConnectionRepository: ICommitFileUserConnectionDao,
  @Autowired private val issueUserConnectionRepository: IIssueUserConnectionDao
) : UserService {

  var logger: Logger = LoggerFactory.getLogger(UserServiceImpl::class.java)

  override fun findAll(pageable: Pageable): Page<User> {
    logger.trace("Getting all users with pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}")
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
    return commitFileUserConnectionRepository.findFilesByUser(userId)
  }
}
