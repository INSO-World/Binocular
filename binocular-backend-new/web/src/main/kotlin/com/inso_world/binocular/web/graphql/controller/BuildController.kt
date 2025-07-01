package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.web.entity.Build
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
import com.inso_world.binocular.web.graphql.model.PageDto
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

  /**
   * Find all builds with pagination and optional timestamp filter.
   * 
   * This method returns a Page object that includes:
   * - count: total number of items
   * - page: current page number (1-based)
   * - perPage: number of items per page
   * - data: list of builds for the current page
   * 
   * @param page The page number (1-based). If null, defaults to 1.
   * @param perPage The number of items per page. If null, defaults to 20.
   * @param until Optional timestamp to filter builds (only include builds before this timestamp)
   * @return A Page object containing the builds and pagination metadata.
   */
  @QueryMapping(name = "builds")
  fun findAll(
    @Argument page: Int?, 
    @Argument perPage: Int?,
    @Argument until: Long?
  ): PageDto<Build> {
    logger.info("Getting builds with page=$page, perPage=$perPage, until=$until")

    val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

    val buildsPage = if (until != null) {
      buildService.findAll(pageable, until)
    } else {
      buildService.findAll(pageable)
    }

    return PageDto(buildsPage)
  }

  /**
   * Find a build by its ID.
   * 
   * This method retrieves a single build based on the provided ID.
   * If no build is found with the given ID, an exception is thrown.
   * 
   * @param id The unique identifier of the build to retrieve.
   * @return The build with the specified ID.
   * @throws GraphQLException if no build is found with the given ID.
   */
  @QueryMapping(name = "build")
  fun findById(@Argument id: String): Build {
    logger.info("Getting build by id: $id")
    return GraphQLValidationUtils.requireEntityExists(buildService.findById(id), "Build", id)
  }
}
