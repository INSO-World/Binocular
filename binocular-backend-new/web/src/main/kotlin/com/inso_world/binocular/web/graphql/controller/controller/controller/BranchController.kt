package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
import com.inso_world.binocular.web.service.BranchService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
@SchemaMapping(typeName = "Branch")
class BranchController(
  @Autowired private val branchService: BranchService,
) {
  private var logger: Logger = LoggerFactory.getLogger(BranchController::class.java)

  @QueryMapping(name = "branches")
  fun findAll(@Argument page: Int?, @Argument perPage: Int?): Iterable<Branch> {
    logger.trace("Getting all branches...")

    GraphQLValidationUtils.validatePagination(page, perPage)

    return branchService.findAll(page, perPage)
  }

  @QueryMapping(name = "branch")
  fun findById(@Argument id: String): Branch {
    logger.trace("Getting branch by id: $id")
    return GraphQLValidationUtils.requireEntityExists(branchService.findById(id), "Branch", id)
  }
}
