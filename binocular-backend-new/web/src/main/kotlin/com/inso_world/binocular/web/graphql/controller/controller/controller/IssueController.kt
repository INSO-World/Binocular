package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
import com.inso_world.binocular.web.service.IssueService
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

  @QueryMapping(name = "issues")
  fun findAll(@Argument page: Int?, @Argument perPage: Int?): Iterable<Issue> {
    logger.trace("Getting all issues...")

    GraphQLValidationUtils.validatePagination(page, perPage)

    return issueService.findAll(page, perPage)
  }

  @QueryMapping(name = "issue")
  fun findById(@Argument id: String): Issue {
    logger.trace("Getting issue by id: $id")
    return GraphQLValidationUtils.requireEntityExists(issueService.findById(id), "Issue", id)
  }
}
