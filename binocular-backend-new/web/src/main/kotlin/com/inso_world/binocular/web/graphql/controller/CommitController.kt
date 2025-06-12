package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
import com.inso_world.binocular.web.graphql.model.PageDto
import com.inso_world.binocular.web.service.CommitService
import com.inso_world.binocular.web.util.PaginationUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller


@Controller
@SchemaMapping(typeName = "Commit")
class CommitController(
  @Autowired private val commitService: CommitService
) {
  private var logger: Logger = LoggerFactory.getLogger(CommitController::class.java)

  /**
   * Find all commits with pagination.
   * 
   * This method returns a Page object that includes:
   * - count: total number of items
   * - page: current page number (1-based)
   * - perPage: number of items per page
   * - data: list of commits for the current page
   * 
   * @param page The page number (1-based). If null, defaults to 1.
   * @param perPage The number of items per page. If null, defaults to 20.
   * @return A Page object containing the commits and pagination metadata.
   */
  @QueryMapping(name = "commits")
  fun findAll(@Argument page: Int?, @Argument perPage: Int?): PageDto<Commit> {
    logger.info("Getting all commits...")

    val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

    val commitsPage = commitService.findAll(pageable)

    return PageDto(commitsPage)
  }

  /**
   * Find a commit by its ID.
   * 
   * This method retrieves a single commit based on the provided ID.
   * If no commit is found with the given ID, an exception is thrown.
   * 
   * @param id The unique identifier of the commit to retrieve.
   * @return The commit with the specified ID.
   * @throws GraphQLException if no commit is found with the given ID.
   */
  @QueryMapping(name = "commit")
  fun findById(@Argument id: String): Commit {
    logger.info("Getting commit by id: $id")
    return GraphQLValidationUtils.requireEntityExists(commitService.findById(id), "Commit", id)
  }
}
