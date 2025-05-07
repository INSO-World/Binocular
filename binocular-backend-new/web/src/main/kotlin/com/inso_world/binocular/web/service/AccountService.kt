package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.AccountDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class AccountService(
  @Autowired private val accountDao: AccountDao,
) {

  var logger: Logger = LoggerFactory.getLogger(AccountService::class.java)

  fun findAll(page: Int? = 1, perPage: Int? = 100): Iterable<Account> {
    logger.trace("Getting all accounts...")
    val page = page ?: 1
    val perPage = perPage ?: 100
    logger.debug("page is $page, perPage is $perPage")
    val pageable: Pageable = PageRequest.of(page, perPage)

    return accountDao.findAll()
  }

  fun findById(id: String): Account? {
    logger.trace("Getting account by id: $id")
    return accountDao.findById(id)
  }
}
