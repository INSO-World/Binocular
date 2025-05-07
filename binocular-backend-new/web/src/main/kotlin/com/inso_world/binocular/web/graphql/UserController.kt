package com.inso_world.binocular.web.graphql

import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.service.UserService
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
  @Autowired private val userService: UserService,
) {
  private var logger: Logger = LoggerFactory.getLogger(UserController::class.java)

  @QueryMapping(name = "users")
  fun findAll(@Argument page: Int?, @Argument perPage: Int?): Iterable<User> {
    logger.trace("Getting all users...")
    return userService.findAll(page, perPage)
  }

  @QueryMapping(name = "user")
  fun findById(@Argument id: String): User? {
    logger.trace("Getting user by id: $id")
    return userService.findById(id)
  }
}
