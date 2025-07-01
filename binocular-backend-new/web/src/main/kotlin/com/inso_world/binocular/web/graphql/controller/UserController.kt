package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
import com.inso_world.binocular.web.graphql.model.PageDto
import com.inso_world.binocular.web.service.UserService
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
  @Autowired private val userService: UserService
) {
  private var logger: Logger = LoggerFactory.getLogger(UserController::class.java)

  /**
   * Find all users with pagination.
   * 
   * This method returns a Page object that includes:
   * - count: total number of items
   * - page: current page number (1-based)
   * - perPage: number of items per page
   * - data: list of users for the current page
   *
   * @param page The page number (1-based). If null, defaults to 1.
   * @param perPage The number of items per page. If null, defaults to 20.
   * @return A Page object containing the users and pagination metadata.
   */
  @QueryMapping(name = "users")
  fun findAll(@Argument page: Int?, @Argument perPage: Int?): PageDto<User> {
    logger.info("Getting all users...")

    val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

    val usersPage = userService.findAll(pageable)

    return PageDto(usersPage)
  }

  @QueryMapping(name = "user")
  fun findById(@Argument id: String): User {
    logger.info("Getting user by id: $id")
    return GraphQLValidationUtils.requireEntityExists(userService.findById(id), "User", id)
  }
}
