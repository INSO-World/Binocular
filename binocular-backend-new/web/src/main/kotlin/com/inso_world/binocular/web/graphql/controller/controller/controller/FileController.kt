package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
import com.inso_world.binocular.web.service.FileService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
@SchemaMapping(typeName = "File")
class FileController(
  @Autowired private val fileService: FileService,
) {
  private var logger: Logger = LoggerFactory.getLogger(FileController::class.java)

  @QueryMapping(name = "files")
  fun findAll(@Argument page: Int?, @Argument perPage: Int?): Iterable<File> {
    logger.trace("Getting all files...")

    GraphQLValidationUtils.validatePagination(page, perPage)

    return fileService.findAll(page, perPage)
  }

  @QueryMapping(name = "file")
  fun findById(@Argument id: String): File {
    logger.trace("Getting file by id: $id")
    return GraphQLValidationUtils.requireEntityExists(fileService.findById(id), "File", id)
  }
}
