package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.BranchFileConnectionRepository
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class BranchResolver(
    private val branchFileConnectionRepository: BranchFileConnectionRepository
) {
    @SchemaMapping(typeName = "Branch", field = "files")
    fun files(branch: Branch): List<File> {
        val id = branch.id ?: return emptyList()
        // Get all files for this branch
        return branchFileConnectionRepository.findFilesByBranch(id)
    }
}
