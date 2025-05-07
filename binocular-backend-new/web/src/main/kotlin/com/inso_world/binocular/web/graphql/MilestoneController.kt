package com.inso_world.binocular.web.graphql

import com.inso_world.binocular.web.entity.Milestone
import com.inso_world.binocular.web.service.MilestoneService
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
    logger.trace("Getting all milestones...")
    return milestoneService.findAll(page, perPage)
  }

  @QueryMapping(name = "milestone")
  fun findById(@Argument id: String): Milestone? {
    logger.trace("Getting milestone by id: $id")
    return milestoneService.findById(id)
  }
}
