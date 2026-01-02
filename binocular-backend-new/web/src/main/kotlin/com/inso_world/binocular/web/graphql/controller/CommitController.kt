package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.model.Commit
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
import java.time.ZoneOffset

@Controller
@SchemaMapping(typeName = "Commit")
class CommitController(
    @Autowired private val commitService: CommitInfrastructurePort,
) {
    private var logger: Logger = LoggerFactory.getLogger(CommitController::class.java)

    /**
     * Find all commits with pagination and optional timestamp filters.
     *
     * This method returns a Page object that includes:
     * - count: total number of items
     * - page: current page number (1-based)
     * - perPage: number of items per page
     * - data: list of commits for the current page
     *
     * @param page The page number (1-based). If null, defaults to 1.
     * @param perPage The number of items per page. If null, defaults to 20.
     * @param since Optional timestamp to filter commits (only include commits after this timestamp)
     * @param until Optional timestamp to filter commits (only include commits before this timestamp)
     * @return A Page object containing the commits and pagination metadata.
     */
    @QueryMapping(name = "commits")
    fun findAll(
        @Argument page: Int?,
        @Argument perPage: Int?,
        @Argument since: Long?,
        @Argument until: Long?,
        @Argument sort: String?,
    ): PageDto<Commit> {
        logger.info("Getting commits with page=$page, perPage=$perPage, since=$since, until=$until")

        val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

        // TODO: should be done in db directly not here
        if (sort != null) {
            fun Commit.commitMillis(): Long? = this.commitDateTime?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
            val comparatorAsc = compareBy<Commit>({ it.commitMillis() }, { it.sha })

            val all = commitService.findAll().toList()
            val filtered = all.asSequence().filter { c ->
                val ts = c.commitMillis() ?: return@filter true
                (since == null || ts >= since) && (until == null || ts <= until)
            }.toList()

            val sorted = when (sort.uppercase()) {
                "ASC" -> filtered.sortedWith(comparatorAsc)
                "DESC" -> filtered.sortedWith(comparatorAsc.reversed())
                else -> filtered
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

        val commitsPage = commitService.findAll(pageable, since, until)
        return PageDto(commitsPage)
    }

    /**
     * Find a commit by its ID.
     *
     * This method retrieves a single commit based on the provided ID.
     * If no commit is found with the given ID, an exception is thrown.
     *
     * @param id The unique identifier of the commit to retrieve.
     * @return The commit with the specified ID.
     * @throws GraphQLException if no commit is found with the given ID.
     */
    @QueryMapping(name = "commit")
    fun findById(
        @Argument id: String,
    ): Commit {
        logger.info("Getting commit by id: $id")
        return GraphQLValidationUtils.requireEntityExists(commitService.findById(id), "Commit", id)
    }
}
