package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
import com.inso_world.binocular.web.service.MergeRequestService
import com.inso_world.binocular.web.util.PaginationUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
@SchemaMapping(typeName = "MergeRequest")
class MergeRequestController(
  @Autowired private val mergeRequestService: MergeRequestService,
) {
  private var logger: Logger = LoggerFactory.getLogger(MergeRequestController::class.java)

  @QueryMapping(name = "mergeRequests")
  fun findAll(@Argument page: Int?, @Argument perPage: Int?): Iterable<MergeRequest> {
    logger.info("Getting all merge requests...")

    val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

    return mergeRequestService.findAll(pageable)
  }

  @QueryMapping(name = "mergeRequest")
  fun findById(@Argument id: String): MergeRequest {
    logger.info("Getting merge request by id: $id")
    return GraphQLValidationUtils.requireEntityExists(mergeRequestService.findById(id), "MergeRequest", id)
  }
}
