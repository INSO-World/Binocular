package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
import com.inso_world.binocular.web.graphql.model.PageDto
import com.inso_world.binocular.web.service.IssueService
import com.inso_world.binocular.web.util.PaginationUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
@SchemaMapping(typeName = "Issue")
class IssueController(
  @Autowired private val issueService: IssueService,
) {
  private var logger: Logger = LoggerFactory.getLogger(IssueController::class.java)

  /**
   * Find all issues with pagination.
   * 
   * This method returns a Page object that includes:
   * - count: total number of items
   * - page: current page number (1-based)
   * - perPage: number of items per page
   * - data: list of issues for the current page
   * 
   * @param page The page number (1-based). If null, defaults to 1.
   * @param perPage The number of items per page. If null, defaults to 20.
   * @return A Page object containing the issues and pagination metadata.
   */
  @QueryMapping(name = "issues")
  fun findAll(@Argument page: Int?, @Argument perPage: Int?): PageDto<Issue> {
    logger.info("Getting all issues...")

    val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

    val issuesPage = issueService.findAll(pageable)

    return PageDto(issuesPage)
  }

  /**
   * Find an issue by its ID.
   * 
   * This method retrieves a single issue based on the provided ID.
   * If no issue is found with the given ID, an exception is thrown.
   * 
   * @param id The unique identifier of the issue to retrieve.
   * @return The issue with the specified ID.
   * @throws GraphQLException if no issue is found with the given ID.
   */
  @QueryMapping(name = "issue")
  fun findById(@Argument id: String): Issue {
    logger.info("Getting issue by id: $id")
    return GraphQLValidationUtils.requireEntityExists(issueService.findById(id), "Issue", id)
  }
}
