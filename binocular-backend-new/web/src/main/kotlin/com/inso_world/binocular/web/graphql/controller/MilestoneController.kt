package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.web.entity.Milestone
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
import com.inso_world.binocular.web.graphql.model.PageDto
import com.inso_world.binocular.web.service.MilestoneService
import com.inso_world.binocular.web.util.PaginationUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
@SchemaMapping(typeName = "Milestone")
class MilestoneController(
  @Autowired private val milestoneService: MilestoneService,
) {
  private var logger: Logger = LoggerFactory.getLogger(MilestoneController::class.java)

  /**
   * Find all milestones with pagination.
   * 
   * This method returns a Page object that includes:
   * - count: total number of items
   * - page: current page number (1-based)
   * - perPage: number of items per page
   * - data: list of milestones for the current page
   * 
   * @param page The page number (1-based). If null, defaults to 1.
   * @param perPage The number of items per page. If null, defaults to 20.
   * @return A Page object containing the milestones and pagination metadata.
   */
  @QueryMapping(name = "milestones")
  fun findAll(@Argument page: Int?, @Argument perPage: Int?): PageDto<Milestone> {
    logger.info("Getting all milestones...")

    val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

    val milestonesPage = milestoneService.findAll(pageable)

    return PageDto(milestonesPage)
  }

  /**
   * Find a milestone by its ID.
   * 
   * This method retrieves a single milestone based on the provided ID.
   * If no milestone is found with the given ID, an exception is thrown.
   * 
   * @param id The unique identifier of the milestone to retrieve.
   * @return The milestone with the specified ID.
   * @throws GraphQLException if no milestone is found with the given ID.
   */
  @QueryMapping(name = "milestone")
  fun findById(@Argument id: String): Milestone {
    logger.info("Getting milestone by id: $id")
    return GraphQLValidationUtils.requireEntityExists(milestoneService.findById(id), "Milestone", id)
  }
}
