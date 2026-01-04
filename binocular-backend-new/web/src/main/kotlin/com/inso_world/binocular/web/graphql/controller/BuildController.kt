package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.core.service.BuildInfrastructurePort
import com.inso_world.binocular.model.Build
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
import com.inso_world.binocular.web.graphql.model.PageDto
import com.inso_world.binocular.web.graphql.model.Sort
import com.inso_world.binocular.web.util.PaginationUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller
import java.time.ZoneOffset

@Controller
@SchemaMapping(typeName = "Build")
class BuildController(
    @Autowired private val buildService: BuildInfrastructurePort,
) {
    private var logger: Logger = LoggerFactory.getLogger(BuildController::class.java)

    /**
     * Find all builds with pagination and optional time-range filtering.
     *
     * @param page The page number (1-based). If null, defaults to 1.
     * @param perPage The number of items per page. If null, defaults to 20.
     * @param since Optional timestamp (epoch millis) to include only builds created at or after this moment.
     * @param until Optional timestamp (epoch millis) to include only builds created at or before this moment.
     * @param sort Optional sort direction (ASC|DESC). Defaults to DESC when not provided.
     * @return A Page object containing the builds and pagination metadata.
     */
    @QueryMapping(name = "builds")
    fun findAll(
        @Argument page: Int?,
        @Argument perPage: Int?,
        @Argument since: Long?,
        @Argument until: Long?,
        @Argument sort: Sort?,
    ): PageDto<Build> {
        logger.info(
            "Getting builds with page={}, perPage={}, since={}, until={}, sort={}",
            page, perPage, since, until, sort
        )

        val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

        return findBuildsInternal(
            pageable = pageable,
            since = since,
            until = until,
            sort = sort
        )
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
    fun findById(
        @Argument id: String,
    ): Build {
        logger.info("Getting build by id: $id")
        return GraphQLValidationUtils.requireEntityExists(buildService.findById(id), "Build", id)
    }

    /**
     * Internal helper that applies optional time-window filtering and sorting, then paginates in-memory.
     *
     * Note: This currently performs filtering/sorting on the application side. Consider moving
     * these concerns into the database layer for performance and consistency.
     *
     * @param pageable Spring pageable (0-based) used to slice the result
     * @param since include only items with createdAt >= since (epoch millis) when provided
     * @param until include only items with createdAt <= until (epoch millis) when provided
     * @param sort desired sort direction (defaults handled by caller)
     * @return a PageDto containing the sliced results and pagination metadata
     */
    // TODO: idk if this is good, should be done in the db i guess
    private fun findBuildsInternal(
        pageable: Pageable,
        since: Long?,
        until: Long?,
        sort: Sort?
    ): PageDto<Build> {

        fun Build.createdMillis() =
            createdAt?.toInstant(ZoneOffset.UTC)?.toEpochMilli()

        val comparatorAsc: Comparator<Build> =
            Comparator { a, b ->
                compareValuesBy(
                    a, b,
                    { it.createdAt?.toInstant(ZoneOffset.UTC)?.toEpochMilli() },
                    { it.committedAt?.toInstant(ZoneOffset.UTC)?.toEpochMilli() },
                    { it.id?.toLongOrNull() ?: Long.MAX_VALUE },
                    { it.id ?: "" }
                )
            }

        val filteredAndSorted =
            buildService.findAll()
                .asSequence()
                .filter { build ->
                    val ts = build.createdMillis() ?: return@filter true
                    (since == null || ts >= since) &&
                            (until == null || ts <= until)
                }
                .let { seq ->
                    val effectiveSort = sort ?: Sort.DESC
                    when (effectiveSort) {
                        Sort.ASC -> seq.sortedWith(comparatorAsc)
                        Sort.DESC -> seq.sortedWith(comparatorAsc.reversed())
                    }
                }
                .toList()

        val from = (pageable.pageNumber * pageable.pageSize)
            .coerceAtMost(filteredAndSorted.size)
        val to = (from + pageable.pageSize)
            .coerceAtMost(filteredAndSorted.size)

        return PageDto(
            count = filteredAndSorted.size,
            pageable = pageable,
            data = filteredAndSorted.subList(from, to)
        )
    }

}
