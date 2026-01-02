package com.inso_world.binocular.web.graphql.model

/**
 * Paginated container for FileInBranch items. Matches GraphQL type PaginatedFileInBranch.
 * Note: current GraphQL schema only exposes the data array for this type.
 */
data class PaginatedFileInBranchDto(
    val count: Int,
    val page: Int,
    val perPage: Int,
    val data: List<FileInBranchDto>,
)
