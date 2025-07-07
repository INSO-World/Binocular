package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.File
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class BranchResolver(
    private val branchService: BranchInfrastructurePort,
) {
    private val logger: Logger = LoggerFactory.getLogger(BranchResolver::class.java)

    /**
     * Resolves the files field for a Branch in GraphQL.
     *
     * This method retrieves all files associated with the given branch.
     * If the branch ID is null, an empty list is returned.
     *
     * @param branch The branch for which to retrieve files
     * @return A list of files associated with the branch, or an empty list if the branch ID is null
     */
    @SchemaMapping(typeName = "Branch", field = "files")
    fun files(branch: Branch): List<File> {
        val id = branch.id ?: return emptyList()
        logger.info("Resolving files for branch: $id")
        // Get all files for this branch
        return branchService.findFilesByBranchId(id)
    }
}
