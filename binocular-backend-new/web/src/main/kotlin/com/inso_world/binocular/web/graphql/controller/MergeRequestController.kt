package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.core.service.MergeRequestInfrastructurePort
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
import com.inso_world.binocular.web.graphql.model.PageDto
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
@SchemaMapping(typeName = "mergeRequest")
class MergeRequestController(
    @Autowired private val mergeRequestService: MergeRequestInfrastructurePort,
) {
    private var logger: Logger = LoggerFactory.getLogger(MergeRequestController::class.java)

    /**
     * Find all merge requests with pagination.
     *
     * This method returns a Page object that includes:
     * - count: total number of items
     * - page: current page number (1-based)
     * - perPage: number of items per page
     * - data: list of merge requests for the current page
     *
     * @param page The page number (1-based). If null, defaults to 1.
     * @param perPage The number of items per page. If null, defaults to 20.
     * @return A Page object containing the merge requests and pagination metadata.
     */
    @QueryMapping(name = "mergeRequests")
    fun findAll(
        @Argument page: Int?,
        @Argument perPage: Int?,
        @Argument since: Long?,
        @Argument until: Long?,
    ): PageDto<MergeRequest> {
        logger.info("Getting all merge requests with page=$page, perPage=$perPage, since=$since, until=$until")

        val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

        return findMergeRequestsInternal(pageable = pageable, since = since, until = until)
    }

    // TODO: fix this and filter in db
    private fun findMergeRequestsInternal(
        pageable: Pageable,
        since: Long?,
        until: Long?,
    ): PageDto<MergeRequest> {
        fun MergeRequest.createdMillis(): Long? = try {
            this.createdAt?.let { java.time.Instant.parse(it).toEpochMilli() }
        } catch (e: Exception) { null }

        val filtered = mergeRequestService.findAll()
            .asSequence()
            .filter { mr ->
                val ts = mr.createdMillis() ?: return@filter true
                (since == null || ts >= since) && (until == null || ts <= until)
            }
            .toList()

        val from = (pageable.pageNumber * pageable.pageSize).coerceAtMost(filtered.size)
        val to = (from + pageable.pageSize).coerceAtMost(filtered.size)

        return PageDto(
            count = filtered.size,
            pageable = pageable,
            data = filtered.subList(from, to)
        )
    }

    /**
     * Find a merge request by its ID.
     *
     * This method retrieves a single merge request based on the provided ID.
     * If no merge request is found with the given ID, an exception is thrown.
     *
     * @param id The unique identifier of the merge request to retrieve.
     * @return The merge request with the specified ID.
     * @throws GraphQLException if no merge request is found with the given ID.
     */
    @QueryMapping(name = "mergeRequest")
    fun findById(
        @Argument id: String,
    ): MergeRequest {
        logger.info("Getting merge request by id: $id")
        return GraphQLValidationUtils.requireEntityExists(mergeRequestService.findById(id), "MergeRequest", id)
    }
}
