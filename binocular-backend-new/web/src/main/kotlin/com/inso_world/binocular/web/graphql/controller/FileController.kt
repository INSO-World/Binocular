package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.core.service.FileInfrastructurePort
import com.inso_world.binocular.model.File
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
import com.inso_world.binocular.web.graphql.model.PageDto
import com.inso_world.binocular.web.graphql.model.Sort
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
     * @param page The page number (1-based). If null, defaults to 1.
     * @param perPage The number of items per page. If null, defaults to 20.
     * @param sort Optional sort direction (ASC|DESC). Defaults to DESC when not provided.
     * @return A Page object containing the files and pagination metadata.
     */
    @QueryMapping(name = "files")
    fun findAll(
        @Argument page: Int?,
        @Argument perPage: Int?,
        @Argument sort: Sort?,
    ): PageDto<File> {
        logger.info("Getting all files... sort={}", sort)

        val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

        val all = fileService.findAll().toList()
        val comparatorAsc = compareBy<File>({ it.path }, { it.id ?: "" })
        val effectiveSort = sort ?: Sort.DESC
        val sorted = when (effectiveSort) {
            Sort.ASC -> all.sortedWith(comparatorAsc)
            Sort.DESC -> all.sortedWith(comparatorAsc.reversed())
        }
        val from = (pageable.pageNumber * pageable.pageSize).coerceAtMost(sorted.size)
        val to = (from + pageable.pageSize).coerceAtMost(sorted.size)
        val slice = if (from < to) sorted.subList(from, to) else emptyList()
        return PageDto(
            count = sorted.size,
            page = pageable.pageNumber + 1,
            perPage = pageable.pageSize,
            data = slice,
        )
    }

    /**
     * Find a file by its ID or path.
     *
     * This method retrieves a single file based on the provided ID.
     * If no file is found with the given ID, an exception is thrown.
     *
     * @param id The unique identifier of the file to retrieve.
     * @param path The unique path of the file to retrieve.
     * @return The file with the specified ID.
     * @throws GraphQLException if no file is found with the given ID.
     */
    @QueryMapping(name = "file")
    fun findByIdOrPath(
        @Argument id: String?,
        @Argument path: String?,
    ): File {
        if (id != null) {
            logger.info("Getting file by id: $id")
            return GraphQLValidationUtils.requireEntityExists(fileService.findById(id), "File", id)
        }
        if (path != null) {
            logger.info("Getting file by path: $path")
            val match = fileService.findByPath(path)
            return GraphQLValidationUtils.requireEntityExists(match, "File", path)
        }
        throw IllegalArgumentException("Either id or path must be provided")
    }

}
