package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
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

  @QueryMapping(name = "commits")
  fun findAll(@Argument page: Int?, @Argument perPage: Int?): Iterable<Commit> {
    logger.info("Getting all commits...")

    val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

    return commitService.findAll(pageable)
  }

    @QueryMapping(name = "commit")
    fun findById(@Argument id: String): Commit {
        logger.info("Getting commit by id: $id")
        return GraphQLValidationUtils.requireEntityExists(commitService.findById(id), "Commit", id)
    }
}
