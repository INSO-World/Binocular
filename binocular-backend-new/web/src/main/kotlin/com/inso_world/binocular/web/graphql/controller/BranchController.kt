package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.model.Branch
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
@SchemaMapping(typeName = "Branch")
class BranchController(
    @Autowired private val branchService: BranchInfrastructurePort,
) {
    private var logger: Logger = LoggerFactory.getLogger(BranchController::class.java)

    /**
     * Find all branches with pagination.
     *
     * This method returns a Page object that includes:
     * - count: total number of items
     * - page: current page number (1-based)
     * - perPage: number of items per page
     * - data: list of branches for the current page
     *
     * @param page The page number (1-based). If null, defaults to 1.
     * @param perPage The number of items per page. If null, defaults to 20.
     * @return A Page object containing the branches and pagination metadata.
     */
    @QueryMapping(name = "branches")
    fun findAll(
        @Argument page: Int?,
        @Argument perPage: Int?,
    ): PageDto<Branch> {
        logger.info("Getting all branches...")

        val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

        val branchesPage = branchService.findAll(pageable)

        return PageDto(branchesPage)
    }

    /**
     * Find a branch by its ID.
     *
     * This method retrieves a single branch based on the provided ID.
     * If no branch is found with the given ID, an exception is thrown.
     *
     * @param id The unique identifier of the branch to retrieve.
     * @return The branch with the specified ID.
     * @throws GraphQLException if no branch is found with the given ID.
     */
    @QueryMapping(name = "branch")
    fun findById(
        @Argument id: String,
    ): Branch {
        logger.info("Getting branch by id: $id")
        return GraphQLValidationUtils.requireEntityExists(branchService.findById(id), "Branch", id)
    }
}
