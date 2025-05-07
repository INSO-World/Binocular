package com.inso_world.binocular.web.graphql

import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
import com.inso_world.binocular.web.service.ModuleService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
@SchemaMapping(typeName = "Module")
class ModuleController(
  @Autowired private val moduleService: ModuleService,
) {
  private var logger: Logger = LoggerFactory.getLogger(ModuleController::class.java)

  @QueryMapping(name = "modules")
  fun findAll(@Argument page: Int?, @Argument perPage: Int?): Iterable<Module> {
    logger.trace("Getting all modules...")

    GraphQLValidationUtils.validatePagination(page, perPage)

    return moduleService.findAll(page, perPage)
  }

  @QueryMapping(name = "module")
  fun findById(@Argument id: String): Module {
    logger.trace("Getting module by id: $id")
    return GraphQLValidationUtils.requireEntityExists(moduleService.findById(id), "Module", id)
  }
}
