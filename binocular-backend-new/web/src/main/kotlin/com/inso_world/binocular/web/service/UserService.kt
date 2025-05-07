package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.UserDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class UserService(
  @Autowired private val userDao: UserDao,
) {

  var logger: Logger = LoggerFactory.getLogger(UserService::class.java)

  fun findAll(page: Int? = 1, perPage: Int? = 100): Iterable<User> {
    logger.trace("Getting all users...")
    val page = page ?: 1
    val perPage = perPage ?: 100
    logger.debug("page is $page, perPage is $perPage")
    val pageable: Pageable = PageRequest.of(page - 1, perPage)

    return userDao.findAll(pageable)
  }

  fun findById(id: String): User? {
    logger.trace("Getting user by id: $id")
    return userDao.findById(id)
  }
}
