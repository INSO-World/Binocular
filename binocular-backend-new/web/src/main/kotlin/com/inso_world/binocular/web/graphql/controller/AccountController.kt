package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.core.service.AccountInfrastructurePort
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
import com.inso_world.binocular.web.graphql.model.PageDto
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
    @Autowired private val accountService: AccountInfrastructurePort,
) {
    private var logger: Logger = LoggerFactory.getLogger(AccountController::class.java)

    /**
     * Find all accounts with pagination.
     *
     * This method returns a Page object that includes:
     * - count: total number of items
     * - page: current page number (1-based)
     * - perPage: number of items per page
     * - data: list of accounts for the current page
     *
     * @param page The page number (1-based). If null, defaults to 1.
     * @param perPage The number of items per page. If null, defaults to 20.
     * @return A Page object containing the accounts and pagination metadata.
     */
    @QueryMapping(name = "accounts")
    fun findAll(
        @Argument page: Int?,
        @Argument perPage: Int?,
    ): PageDto<Account> {
        logger.info("Getting all accounts...")

        val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

        val accountsPage = accountService.findAll(pageable)

        return PageDto(accountsPage)
    }

    /**
     * Find an account by its ID.
     *
     * This method retrieves a single account based on the provided ID.
     * If no account is found with the given ID, an exception is thrown.
     *
     * @param id The unique identifier of the account to retrieve.
     * @return The account with the specified ID.
     * @throws GraphQLException if no account is found with the given ID.
     */
    @QueryMapping(name = "account")
    fun findById(
        @Argument id: String,
    ): Account {
        logger.info("Getting account by id: $id")
        return GraphQLValidationUtils.requireEntityExists(accountService.findById(id), "Account", id)
    }
}
