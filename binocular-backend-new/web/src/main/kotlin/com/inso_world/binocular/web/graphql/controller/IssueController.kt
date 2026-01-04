package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.core.service.IssueInfrastructurePort
import com.inso_world.binocular.model.Issue
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

@Controller
@SchemaMapping(typeName = "Issue")
class IssueController(
    @Autowired private val issueService: IssueInfrastructurePort,
) {
    private var logger: Logger = LoggerFactory.getLogger(IssueController::class.java)

    /**
     * Find all issues with pagination and optional time-range filtering.
     *
     * @param page The page number (1-based). If null, defaults to 1.
     * @param perPage The number of items per page. If null, defaults to 20.
     * @param since Optional timestamp (epoch millis) to include only issues created at or after this moment.
     * @param until Optional timestamp (epoch millis) to include only issues created at or before this moment.
     * @param sort Optional sort direction (ASC|DESC). Defaults to ASC when not provided.
     * @return A Page object containing the issues and pagination metadata.
     */
    @QueryMapping(name = "issues")
    fun findAll(
        @Argument page: Int?,
        @Argument perPage: Int?,
        @Argument since: Long?,
        @Argument until: Long?,
        @Argument sort: Sort?,
    ): PageDto<Issue> {
        logger.info("Getting all issues with page=$page, perPage=$perPage, since=$since, until=$until, sort=$sort")

        val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

        return findIssuesInternal(pageable = pageable, since = since, until = until, sort = sort)
    }

    // TODO: filter in db not here
    private fun findIssuesInternal(
        pageable: Pageable,
        since: Long?,
        until: Long?,
        sort: Sort?,
    ): PageDto<Issue> {
        fun Issue.createdMillis(): Long? = this.createdAt
            ?.atOffset(java.time.ZoneOffset.UTC)
            ?.toInstant()
            ?.toEpochMilli()

        val base = issueService.findAll()
            .asSequence()
            .filter { issue ->
                val ts = issue.createdMillis() ?: return@filter true
                (since == null || ts >= since) && (until == null || ts <= until)
            }
        val comparatorAsc = compareBy<Issue>({ it.createdAt }, { it.id ?: "" })
        val effectiveSort = sort ?: Sort.ASC
        val sorted = when (effectiveSort) {
            Sort.ASC -> base.sortedWith(comparatorAsc)
            Sort.DESC -> base.sortedWith(comparatorAsc.reversed())
        }.toList()

        val from = (pageable.pageNumber * pageable.pageSize).coerceAtMost(sorted.size)
        val to = (from + pageable.pageSize).coerceAtMost(sorted.size)

        return PageDto(
            count = sorted.size,
            pageable = pageable,
            data = sorted.subList(from, to)
        )
    }

    /**
     * Find an issue by its ID.
     *
     * This method retrieves a single issue based on the provided ID.
     * If no issue is found with the given ID, an exception is thrown.
     *
     * @param id The unique identifier of the issue to retrieve.
     * @return The issue with the specified ID.
     * @throws GraphQLException if no issue is found with the given ID.
     */
    @QueryMapping(name = "issue")
    fun findById(
        @Argument id: String,
    ): Issue {
        logger.info("Getting issue by id: $id")
        return GraphQLValidationUtils.requireEntityExists(issueService.findById(id), "Issue", id)
    }

}
