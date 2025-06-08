package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.web.entity.Milestone
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
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

  @QueryMapping(name = "milestones")
  fun findAll(@Argument page: Int?, @Argument perPage: Int?): Iterable<Milestone> {
    logger.info("Getting all milestones...")

    val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

    return milestoneService.findAll(pageable)
  }

  @QueryMapping(name = "milestone")
  fun findById(@Argument id: String): Milestone {
    logger.info("Getting milestone by id: $id")
    return GraphQLValidationUtils.requireEntityExists(milestoneService.findById(id), "Milestone", id)
  }
}
