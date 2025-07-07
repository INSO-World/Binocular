package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.core.service.FileInfrastructurePort
import com.inso_world.binocular.model.File
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
import com.inso_world.binocular.web.graphql.model.PageDto
import com.inso_world.binocular.web.util.PaginationUtils
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
    @Autowired private val fileService: FileInfrastructurePort,
) {
    private var logger: Logger = LoggerFactory.getLogger(FileController::class.java)

    /**
     * Find all files with pagination.
     *
     * This method returns a Page object that includes:
     * - count: total number of items
     * - page: current page number (1-based)
     * - perPage: number of items per page
     * - data: list of files for the current page
     *
     * @param page The page number (1-based). If null, defaults to 1.
     * @param perPage The number of items per page. If null, defaults to 20.
     * @return A Page object containing the files and pagination metadata.
     */
    @QueryMapping(name = "files")
    fun findAll(
        @Argument page: Int?,
        @Argument perPage: Int?,
    ): PageDto<File> {
        logger.info("Getting all files...")

        val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

        val filesPage = fileService.findAll(pageable)

        return PageDto(filesPage)
    }

    /**
     * Find a file by its ID.
     *
     * This method retrieves a single file based on the provided ID.
     * If no file is found with the given ID, an exception is thrown.
     *
     * @param id The unique identifier of the file to retrieve.
     * @return The file with the specified ID.
     * @throws GraphQLException if no file is found with the given ID.
     */
    @QueryMapping(name = "file")
    fun findById(
        @Argument id: String,
    ): File {
        logger.info("Getting file by id: $id")
        return GraphQLValidationUtils.requireEntityExists(fileService.findById(id), "File", id)
    }
}
