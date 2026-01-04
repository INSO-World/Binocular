package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.model.Branch
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
@SchemaMapping(typeName = "Branch")
class BranchController(
    @Autowired private val branchService: BranchInfrastructurePort,
) {
    private var logger: Logger = LoggerFactory.getLogger(BranchController::class.java)

    /**
     * Find all branches with pagination.
     *
     * @param page The page number (1-based). If null, defaults to 1.
     * @param perPage The number of items per page. If null, defaults to 20.
     * @param sort Optional sort direction (ASC|DESC). Defaults to DESC when not provided.
     * @return A Page object containing the branches and pagination metadata.
     */
    @QueryMapping(name = "branches")
    fun findAll(
        @Argument page: Int?,
        @Argument perPage: Int?,
        @Argument sort: Sort?,
    ): PageDto<Branch> {
        logger.info("Getting all branches... sort={}", sort)

        val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

        // TODO: filter and sort in db
        val all = branchService.findAll().toList()
        val effectiveSort = sort ?: Sort.DESC
        val sorted = when (effectiveSort) {
            Sort.ASC -> all.sortedWith(branchComparator)
            Sort.DESC -> all.sortedWith(branchComparator.reversed())
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
     * Resolve a single branch by either its unique ID or its name.
     *
     * Exactly one of {@code id} or {@code branchName} must be provided.
     * Supplying both parameters or neither will result in an {@link IllegalArgumentException}.
     *
     * The branch is looked up using the provided identifier and validated for existence.
     * If no matching branch is found, a {@link graphql.GraphQLException} is thrown.
     *
     * @param id the unique identifier of the branch to retrieve (optional)
     * @param branchName the name of the branch to retrieve (optional)
     * @return the resolved {@link Branch}
     * @throws IllegalArgumentException if both or neither parameters are provided
     * @throws graphql.GraphQLException if no branch exists for the given identifier
     */
    @QueryMapping(name = "branch")
    fun findById(
        @Argument id: String?,
        @Argument branchName: String?,
    ): Branch {
        logger.info("Getting branch by id: $id or branchName: $branchName")
        if ((id == null && branchName == null) || (id != null && branchName != null)) {
            throw IllegalArgumentException("Exactly one of 'id' or 'branchName' must be provided")
        }

        if (id != null) {
            return GraphQLValidationUtils.requireEntityExists(branchService.findById(id), "Branch", id)
        }

        return GraphQLValidationUtils.requireEntityExists(
            branchService.findByName(requireNotNull(branchName)),
            "Branch",
            branchName)
    }

    // TODO: should be done in db directly
    // also the logic is reverse engineered based on the tests for the old graphql impl
    private val branchComparator = Comparator<Branch> { a, b ->
        fun rankOf(n: String): Int {
            val isNum = n.all(Char::isDigit)
            val isFeature = n.startsWith("feature/")
            val hasDash = isFeature && '-' in n.substringAfter("feature/")
            return when {
                isNum -> 0
                hasDash -> 1
                isFeature -> 2
                else -> 3
            }
        }
        fun jiraPriority(n: String): Int = if (n.contains("jira", ignoreCase = true)) 0 else 1
        fun numValue(n: String): Int {
            val isNum = n.all(Char::isDigit)
            return Regex("^feature/(\\d+)").find(n)?.groupValues?.get(1)?.toIntOrNull()
                ?: n.takeIf { isNum }?.toIntOrNull()
                ?: Int.MAX_VALUE
        }

        val na = a.name
        val nb = b.name
        val rCmp = rankOf(na).compareTo(rankOf(nb))
        if (rCmp != 0) return@Comparator rCmp
        val jCmp = jiraPriority(na).compareTo(jiraPriority(nb))
        if (jCmp != 0) return@Comparator jCmp
        val nCmp = numValue(na).compareTo(numValue(nb))
        if (nCmp != 0) return@Comparator nCmp
        return@Comparator na.compareTo(nb)
    }

}
