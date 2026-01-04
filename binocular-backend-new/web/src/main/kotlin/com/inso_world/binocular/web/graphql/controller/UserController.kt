package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.model.User
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
@SchemaMapping(typeName = "User")
class UserController(
    @Autowired private val userService: UserInfrastructurePort,
) {
    private var logger: Logger = LoggerFactory.getLogger(UserController::class.java)

    /**
     * Find all users with pagination.
     *
     * @param page The page number (1-based). If null, defaults to 1.
     * @param perPage The number of items per page. If null, defaults to 20.
     * @param sort Optional sort direction (ASC|DESC). Defaults to DESC when not provided.
     * @return A Page object containing the users and pagination metadata.
     */
    @QueryMapping(name = "users")
    fun findAll(
        @Argument page: Int?,
        @Argument perPage: Int?,
        @Argument sort: Sort?,
    ): PageDto<User> {
        logger.info("Getting all users... sort={}", sort)

        val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

        val all = userService.findAll().toList()
        val comparatorAsc = compareBy<User>({ it.name ?: "" }, { it.id ?: "" })
        val effectiveSort = sort ?: Sort.DESC
        val sorted = when (effectiveSort) {
            Sort.ASC -> all.sortedWith(comparatorAsc)
            Sort.DESC -> all.sortedWith(comparatorAsc.reversed())
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
     * Find a user by its ID.
     *
     * Retrieves a single user based on the provided unique identifier. If the user cannot be found,
     * a GraphQLException is thrown.
     *
     * @param id the unique identifier of the user to retrieve
     * @return the user with the specified ID
     * @throws graphql.GraphQLException if no user exists with the given ID
     */
    @QueryMapping(name = "user")
    fun findById(
        @Argument id: String,
    ): User {
        logger.info("Getting user by id: $id")
        return GraphQLValidationUtils.requireEntityExists(userService.findById(id), "User", id)
    }

}
