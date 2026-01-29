package com.inso_world.binocular.web.graphql.model

/**
 * Paginated container for CommitInFile items.
 */
data class PaginatedCommitInFileDto(
    val count: Int,
    val page: Int,
    val perPage: Int,
    val data: List<CommitInFile>,
)
