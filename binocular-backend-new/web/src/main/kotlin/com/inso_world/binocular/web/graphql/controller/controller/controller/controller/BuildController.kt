package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.web.entity.Build
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
import com.inso_world.binocular.web.service.BuildService
import com.inso_world.binocular.web.util.PaginationUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
@SchemaMapping(typeName = "Build")
class BuildController(
  @Autowired private val buildService: BuildService,
) {
  private var logger: Logger = LoggerFactory.getLogger(BuildController::class.java)

  @QueryMapping(name = "builds")
  fun findAll(@Argument page: Int?, @Argument perPage: Int?): Iterable<Build> {
    logger.info("Getting all builds...")

    val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

    return buildService.findAll(pageable)
  }

  @QueryMapping(name = "build")
  fun findById(@Argument id: String): Build {
    logger.info("Getting build by id: $id")
    return GraphQLValidationUtils.requireEntityExists(buildService.findById(id), "Build", id)
  }
}
