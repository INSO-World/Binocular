package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
import com.inso_world.binocular.web.service.AccountService
import com.inso_world.binocular.web.util.PaginationUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
@SchemaMapping(typeName = "Account")
class AccountController(
  @Autowired private val accountService: AccountService,
) {
  private var logger: Logger = LoggerFactory.getLogger(AccountController::class.java)

  @QueryMapping(name = "accounts")
  fun findAll(@Argument page: Int?, @Argument perPage: Int?): Iterable<Account> {
    logger.info("Getting all accounts...")

    val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

    return accountService.findAll(pageable)
  }

  @QueryMapping(name = "account")
  fun findById(@Argument id: String): Account {
    logger.info("Getting account by id: $id")
    return GraphQLValidationUtils.requireEntityExists(accountService.findById(id), "Account", id)
  }
}
