package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.core.service.AccountInfrastructurePort
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
import com.inso_world.binocular.web.graphql.model.PageDto
import com.inso_world.binocular.web.graphql.model.Sort
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
     * @param page The page number (1-based). If null, defaults to 1.
     * @param perPage The number of items per page. If null, defaults to 20.
     * @param sort Optional sort direction (ASC|DESC). Defaults to ASC when not provided.
     * @return A Page object containing the accounts and pagination metadata.
     */
    @QueryMapping(name = "accounts")
    fun findAll(
        @Argument page: Int?,
        @Argument perPage: Int?,
        @Argument sort: Sort?,
    ): PageDto<Account> {
        logger.info("Getting all accounts... sort={}", sort)

        val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

        val all = accountService.findAll().toList()

        // Legacy ordering expected by visualization snapshots:
        // 1) login (case-insensitive)
        // 2) name (case-insensitive)
        // 3) id
        fun idNumKey(a: Account): Long = a.id?.toLongOrNull() ?: Long.MAX_VALUE
        fun idStrKey(a: Account): String = a.id ?: "\uFFFF"
        fun loginKey(a: Account): String = a.login?.lowercase() ?: "\uFFFF"
        val comparatorAsc = compareBy<Account>(
            { idNumKey(it) },
            { idStrKey(it) },
            { loginKey(it) }
        )
        val effectiveSort = sort ?: Sort.ASC
        val sorted = when (effectiveSort) {
            Sort.ASC -> all.sortedWith(comparatorAsc)
            Sort.DESC -> all.sortedWith(comparatorAsc.reversed())
        }
        run {
            val preview = sorted.take(5).joinToString { it.login ?: "<null>" }
            logger.info("Accounts sorted (effectiveSort={}): firstLogins=[{}]", effectiveSort, preview)
        }
        val from = (pageable.pageNumber * pageable.pageSize).coerceAtMost(sorted.size)
        val to = (from + pageable.pageSize).coerceAtMost(sorted.size)
        val slice = if (from < to) sorted.subList(from, to) else emptyList()
        return PageDto(
            count = sorted.size,
            page = pageable.pageNumber + 1,
            perPage = pageable.pageSize,
            data = slice,
        )
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
