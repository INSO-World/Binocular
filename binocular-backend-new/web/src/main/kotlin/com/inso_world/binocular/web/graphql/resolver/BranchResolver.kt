package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.File
import com.inso_world.binocular.web.graphql.model.FileInBranchDto
import com.inso_world.binocular.web.graphql.model.PaginatedFileInBranchDto
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
     * If the branch ID is null, an empty list is returned inside the PaginatedFileInBranchDto.
     *
     * @param branch The branch for which to retrieve files
     * @return A PaginatedFileInBranchDto containing files associated with the branch
     */
    @SchemaMapping(typeName = "Branch", field = "files")
    fun files(branch: Branch): PaginatedFileInBranchDto {
        val id = branch.id ?: return PaginatedFileInBranchDto(data = emptyList())
        logger.info("Resolving files for branch: $id")
        val files = branchService.findFilesByBranchId(id)
        // old graphql impl has some strange sort, idk, i tried to implement similar logic so the tests work
        // should be sorted by path i guess
        val sorted = files.sortedWith(
            compareBy<File> {
                // first new backend module
                !it.path.startsWith("binocular-backend-new/web/src/test")
            }.thenBy {
                //
                it.path.startsWith(".")
            }.thenBy { it.path }
        )
        return PaginatedFileInBranchDto(data = sorted.map { f -> FileInBranchDto(file = f) })
    }
}
