package com.inso_world.binocular.web.graphql.model

import com.inso_world.binocular.model.File

/**
 * Wrapper representing a file within a branch.
 */
data class FileInBranchDto(
    val file: File,
)
