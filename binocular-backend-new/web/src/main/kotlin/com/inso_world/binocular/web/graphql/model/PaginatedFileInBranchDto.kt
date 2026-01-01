package com.inso_world.binocular.web.graphql.model

/**
 * Paginated container for FileInBranch items. Matches GraphQL type PaginatedFileInBranch.
 * Note: current GraphQL schema only exposes the data array for this type.
 */
data class PaginatedFileInBranchDto(
    val data: List<FileInBranchDto>,
)
